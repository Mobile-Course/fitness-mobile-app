package com.fitness.app.ui.screens.post

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.viewModelScope
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
    val selectedImageUri: android.net.Uri? = null
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
                    val fields = mutableMapOf<String, RequestBody>()
                    textPart(current.title)?.let { fields["title"] = it }
                    textPart(current.description)?.let { fields["description"] = it }
                    fields["likesNumber"] = "0".toRequestBody("text/plain".toMediaType())

                    val workoutDetails = buildWorkoutDetails(current, durationValue, caloriesValue)
                    if (workoutDetails.isNotEmpty()) {
                        fields["workoutDetails"] =
                            gson.toJson(workoutDetails).toRequestBody("text/plain".toMediaType())
                    }

                    val filePart =
                        current.selectedImageUri?.let { uri ->
                            createFilePart(context = context, uri = uri)
                        }
                    if (current.selectedImageUri != null && filePart == null) {
                        return@withContext Result.failure(
                            Exception("Image must be JPG/JPEG/PNG/GIF/WEBP and up to 5MB")
                        )
                    }

                    postsRepository.createPost(fields = fields, file = filePart)
                }

            result.fold(
                onSuccess = {
                    updateState { PostUiState() }
                    onSuccess()
                },
                onFailure = { error ->
                    updateState { it.copy(isPosting = false, error = error.message ?: "Failed to create post") }
                }
            )
        }
    }

    private fun buildWorkoutDetails(
        state: PostUiState,
        durationValue: Int?,
        caloriesValue: Int?
    ): Map<String, Any> {
        val details = mutableMapOf<String, Any>()
        if (state.workoutType.isNotBlank()) details["type"] = state.workoutType.trim()
        if (durationValue != null) details["duration"] = durationValue
        if (caloriesValue != null) details["calories"] = caloriesValue
        if (state.subjectiveFeedbackFeelings.isNotBlank()) {
            details["subjectiveFeedbackFeelings"] = state.subjectiveFeedbackFeelings.trim()
        }
        if (state.personalGoals.isNotBlank()) {
            details["personalGoals"] = state.personalGoals.trim()
        }
        return details
    }

    private fun textPart(value: String?): RequestBody? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isBlank()) return null
        return trimmed.toRequestBody("text/plain".toMediaType())
    }

    private fun createFilePart(context: Context, uri: Uri): MultipartBody.Part? {
        return try {
            val resolver = context.contentResolver
            val mimeType = resolver.getType(uri) ?: "image/*"
            if (mimeType.lowercase() !in allowedMimeTypes) return null
            val name = queryFileName(context, uri) ?: "post_image.jpg"
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            if (bytes.size.toLong() > maxUploadBytes) return null
            val body = bytes.toRequestBody(mimeType.toMediaType())
            MultipartBody.Part.createFormData("file", name, body)
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
}
