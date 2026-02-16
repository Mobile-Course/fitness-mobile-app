package com.fitness.app.ui.screens.post

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.viewModelScope
import com.fitness.app.data.model.CreatePostRequest
import com.fitness.app.data.model.CreateWorkoutDetailsRequest
import com.fitness.app.data.repository.PostsRepository
import com.fitness.app.ui.base.BaseViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.FileNotFoundException

/**
 * UI State for the Post Screen.
 * Define all the data that your UI needs to display here.
 */
data class PostUiState(
    val title: String = "",
    val description: String = "",
    val workoutType: String = "",
    val duration: String = "",
    val calories: String = "",
    val subjectiveFeedbackFeelings: String = "",
    val personalGoals: String = "",
    val currentStep: Int = 1,
    val isPosting: Boolean = false,
    val error: String? = null,
    val selectedImageUri: android.net.Uri? = null,
    val isEditing: Boolean = false,
    val editPostId: String? = null
)

/**
 * ViewModel for the Post Screen.
 * This class should handle all business logic, such as:
 * - Validating the post content
 * - Communicating with a repository to save the post
 * - Managing the UI state
 */
class PostViewModel : BaseViewModel<PostUiState>(PostUiState()) {
    private val postsRepository = PostsRepository()
    private val gson = Gson()
    private val maxUploadBytes = 5 * 1024 * 1024L
    private val allowedMimeTypes =
        setOf("image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp")

    fun onTitleChanged(newTitle: String) {
        updateState { it.copy(title = newTitle, error = null) }
    }

    fun onDescriptionChanged(newDescription: String) {
        updateState { it.copy(description = newDescription, error = null) }
    }

    fun onWorkoutTypeChanged(newWorkoutType: String) {
        updateState { it.copy(workoutType = newWorkoutType, error = null) }
    }

    fun onDurationChanged(newDuration: String) {
        updateState { it.copy(duration = newDuration.filter(Char::isDigit), error = null) }
    }

    fun onCaloriesChanged(newCalories: String) {
        updateState { it.copy(calories = newCalories.filter(Char::isDigit), error = null) }
    }

    fun onSubjectiveFeedbackFeelingsChanged(value: String) {
        updateState { it.copy(subjectiveFeedbackFeelings = value, error = null) }
    }

    fun onPersonalGoalsChanged(value: String) {
        updateState { it.copy(personalGoals = value, error = null) }
    }

    fun onImageSelected(uri: android.net.Uri?) {
        updateState { it.copy(selectedImageUri = uri, error = null) }
    }

    fun loadPost(postId: String) {
        if (uiState.value.isEditing && uiState.value.editPostId == postId) return
        updateState { it.copy(isPosting = true, error = null) }
        viewModelScope.launch {
            postsRepository.getPost(postId)
                .onSuccess { post ->
                    updateState {
                        PostUiState(
                            title = post.title,
                            description = post.description ?: "",
                            workoutType = post.workoutDetails?.type ?: "",
                            duration = post.workoutDetails?.duration?.toString() ?: "",
                            calories = post.workoutDetails?.calories?.toString() ?: "",
                            subjectiveFeedbackFeelings = post.workoutDetails?.subjectiveFeedbackFeelings ?: "",
                            personalGoals = post.workoutDetails?.personalGoals ?: "",
                            isEditing = true,
                            editPostId = post.id
                        )
                    }
                }
                .onFailure {
                    updateState { it.copy(isPosting = false, error = "Failed to load post") }
                }
        }
    }

    fun nextStep(): Boolean {
        val title = uiState.value.title.trim()
        if (title.isBlank()) {
            updateState { it.copy(error = "Title is required") }
            return false
        }
        updateState { it.copy(currentStep = 2, error = null) }
        return true
    }

    fun previousStep() {
        updateState { it.copy(currentStep = 1, error = null) }
    }

    fun resetForm() {
        updateState { PostUiState() }
    }

    fun clearError() {
        updateState { it.copy(error = null) }
    }

    /**
     * Logic for submitting the post.
     */
    fun submitPost(context: Context, onSuccess: () -> Unit) {
        val current = uiState.value
        if (current.title.trim().isBlank()) {
            updateState { it.copy(error = "Title is required") }
            return
        }

        val durationValue = current.duration.trim().takeIf { it.isNotBlank() }?.toIntOrNull()
        if (current.duration.isNotBlank() && durationValue == null) {
            updateState { it.copy(error = "Duration must be a valid number") }
            return
        }

        val caloriesValue = current.calories.trim().takeIf { it.isNotBlank() }?.toIntOrNull()
        if (current.calories.isNotBlank() && caloriesValue == null) {
            updateState { it.copy(error = "Calories must be a valid number") }
            return
        }

        updateState { it.copy(isPosting = true, error = null) }

        viewModelScope.launch {
            val result =
                withContext(Dispatchers.IO) {
                    val workoutDetails =
                        buildWorkoutDetailsRequest(current, durationValue, caloriesValue)
                    val filePart =
                        current.selectedImageUri?.let { uri ->
                            createFilePart(context = context, uri = uri)
                        }
                    if (current.selectedImageUri != null && filePart == null) {
                        return@withContext Result.failure(
                            Exception("Image must be JPG/JPEG/PNG/GIF/WEBP and up to 5MB")
                        )
                    }

                    if (filePart == null) {
                        val request = CreatePostRequest(
                            title = current.title.trim(),
                            description = current.description,
                            pictures = emptyList(),
                            workoutDetails = workoutDetails
                        )
                        if (current.isEditing && current.editPostId != null) {
                            postsRepository.updatePost(current.editPostId, request)
                        } else {
                            postsRepository.createPost(request)
                        }
                    } else {
                        val fields = mutableMapOf<String, RequestBody>()
                        fields["title"] = current.title.trim().toRequestBody("text/plain".toMediaType())
                        fields["description"] = current.description.toRequestBody("text/plain".toMediaType())
                        if (!isEmptyWorkoutDetails(workoutDetails)) {
                            fields["workoutDetails"] =
                                gson.toJson(workoutDetails).toRequestBody("text/plain".toMediaType())
                        }

                        // Multipart update not supported for text-only updates in this flow yet
                        // Assumption: Edit mode currently supports text updates via JSON
                        if (current.isEditing) {
                             return@withContext Result.failure(Exception("Image update not supported in edit mode yet"))
                        }
                        postsRepository.createPostMultipart(fields = fields, file = filePart)
                    }
                }

            result.fold(
                onSuccess = {
                    com.fitness.app.utils.DataInvalidator.refreshProfile.value = true
                    com.fitness.app.utils.DataInvalidator.refreshFeed.value = true
                    updateState { PostUiState() }
                    onSuccess()
                },
                onFailure = { error ->
                    val raw = error.message.orEmpty()
                    val isServer500 = raw.contains("500") || raw.contains("Internal server error")
                    val msg =
                        when {
                            current.selectedImageUri != null && isServer500 ->
                                "Upload failed on server (500). If text-only post works, backend upload path may be misconfigured (e.g., missing uploads/posts folder)."
                            raw.isNotBlank() -> raw
                            else -> "Failed to create post. Try again without image or optional details."
                        }
                    updateState { it.copy(isPosting = false, error = msg) }
                }
            )
        }
    }

    private fun buildWorkoutDetailsRequest(
        state: PostUiState,
        durationValue: Int?,
        caloriesValue: Int?
    ): CreateWorkoutDetailsRequest {
        return CreateWorkoutDetailsRequest(
            type = state.workoutType.trim().ifBlank { null },
            duration = durationValue,
            calories = caloriesValue,
            subjectiveFeedbackFeelings = state.subjectiveFeedbackFeelings.trim().ifBlank { null },
            personalGoals = state.personalGoals.trim().ifBlank { null }
        )
    }

    private fun isEmptyWorkoutDetails(details: CreateWorkoutDetailsRequest): Boolean {
        return details.type == null &&
            details.duration == null &&
            details.calories == null &&
            details.subjectiveFeedbackFeelings == null &&
            details.personalGoals == null
    }

    private fun createFilePart(context: Context, uri: Uri): MultipartBody.Part? {
        return try {
            val resolver = context.contentResolver
            val rawName = queryFileName(context, uri) ?: "post_image"
            val mimeType = resolveMimeType(resolver.getType(uri), rawName) ?: return null
            if (mimeType.lowercase() !in allowedMimeTypes) return null
            val name = ensureImageExtension(rawName, mimeType)
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            if (bytes.size.toLong() > maxUploadBytes) return null
            val body = bytes.toRequestBody(mimeType.toMediaType())
            MultipartBody.Part.createFormData("files", name, body)
        } catch (_: FileNotFoundException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun queryFileName(context: Context, uri: Uri): String? {
        val resolver = context.contentResolver
        val cursor = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?: return null
        cursor.use {
            return if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) it.getString(index) else null
            } else {
                null
            }
        }
    }

    private fun ensureImageExtension(fileName: String, mimeType: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        if (dotIndex > 0 && dotIndex < fileName.length - 1) {
            val base = fileName.substring(0, dotIndex)
            val ext = fileName.substring(dotIndex + 1).lowercase()
            if (ext in setOf("jpg", "jpeg", "png", "gif", "webp")) {
                return "$base.$ext"
            }
        }

        val ext =
            when (mimeType.lowercase()) {
                "image/png" -> ".png"
                "image/gif" -> ".gif"
                "image/webp" -> ".webp"
                "image/jpeg", "image/jpg" -> ".jpg"
                else -> ".jpg"
            }
        return "$fileName$ext"
    }

    private fun resolveMimeType(rawMimeType: String?, fileName: String): String? {
        val mime = rawMimeType?.trim()?.lowercase()
        if (!mime.isNullOrBlank() && mime != "image/*") return mime

        return when {
            fileName.endsWith(".png", ignoreCase = true) -> "image/png"
            fileName.endsWith(".gif", ignoreCase = true) -> "image/gif"
            fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
            fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            fileName.endsWith(".jpg", ignoreCase = true) -> "image/jpeg"
            else -> "image/jpeg"
        }
    }
}
