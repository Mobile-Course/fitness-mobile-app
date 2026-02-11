package com.fitness.app.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.fitness.app.auth.UserSession
import com.fitness.app.data.local.AppDatabase
import com.fitness.app.data.model.UpdateUserProfileRequest
import com.fitness.app.data.model.UserPreferencesDto
import com.fitness.app.data.model.extractId
import com.fitness.app.data.model.fullName
import com.fitness.app.data.model.toUserEntity
import com.fitness.app.data.repository.AuthRepository
import com.fitness.app.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

data class EditProfileUiState(
    val password: String = "",
    val name: String = "",
    val lastName: String = "",
    val picture: String = "",
    val imageUri: String? = null,
    val sportType: String = "",
    val weeklyGoal: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class EditProfileViewModel : BaseViewModel<EditProfileUiState>(EditProfileUiState()) {
    private val authRepository = AuthRepository()

    init {
        viewModelScope.launch {
            UserSession.name.collect { name ->
                if (!name.isNullOrBlank()) {
                    updateState { current ->
                        if (current.name.isBlank()) current.copy(name = name) else current
                    }
                }
            }
        }
        viewModelScope.launch {
            UserSession.picture.collect { picture ->
                if (!picture.isNullOrBlank()) {
                    updateState { current ->
                        if (current.picture.isBlank()) current.copy(picture = picture) else current
                    }
                }
            }
        }
        viewModelScope.launch {
            UserSession.bio.collect { bio ->
                if (!bio.isNullOrBlank()) {
                    updateState { current ->
                        if (current.description.isBlank()) current.copy(description = bio) else current
                    }
                }
            }
        }
        refreshProfile()
    }

    private fun refreshProfile() {
        viewModelScope.launch {
            val result =
                withContext(Dispatchers.IO) {
                    authRepository.getProfile()
                }
            result.onSuccess { profile ->
                updateState { current ->
                    current.copy(
                        name = current.name.ifBlank { profile.name.orEmpty() },
                        lastName = current.lastName.ifBlank { profile.lastName.orEmpty() },
                        picture = current.picture.ifBlank { profile.picture.orEmpty() },
                        sportType = current.sportType.ifBlank { profile.sportType.orEmpty() },
                        weeklyGoal =
                            profile.preferences?.weeklyGoal?.toString() ?: current.weeklyGoal,
                        description = current.description.ifBlank { profile.description.orEmpty() }
                    )
                }
            }
        }
    }

    fun onPasswordChanged(value: String) {
        updateState { it.copy(password = value, errorMessage = null) }
    }

    fun onNameChanged(value: String) {
        updateState { it.copy(name = value, errorMessage = null) }
    }

    fun onLastNameChanged(value: String) {
        updateState { it.copy(lastName = value, errorMessage = null) }
    }

    fun onPictureChanged(value: String) {
        updateState { it.copy(picture = value, errorMessage = null) }
    }

    fun onImageSelected(uri: Uri?) {
        updateState {
            it.copy(imageUri = uri?.toString(), errorMessage = null)
        }
    }

    fun onSportTypeChanged(value: String) {
        updateState { it.copy(sportType = value, errorMessage = null) }
    }

    fun onWeeklyGoalChanged(value: String) {
        updateState { it.copy(weeklyGoal = value, errorMessage = null) }
    }

    fun onDescriptionChanged(value: String) {
        updateState { it.copy(description = value, errorMessage = null) }
    }

    fun submit(context: Context, onSuccess: () -> Unit) {
        val weeklyGoalText = uiState.value.weeklyGoal.trim()
        val weeklyGoal =
            if (weeklyGoalText.isBlank()) null
            else weeklyGoalText.toIntOrNull()

        if (weeklyGoalText.isNotBlank() && weeklyGoal == null) {
            updateState { it.copy(errorMessage = "Weekly goal must be a number") }
            return
        }

        updateState { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val imageUri = uiState.value.imageUri?.let { Uri.parse(it) }
            val result =
                withContext(Dispatchers.IO) {
                    if (imageUri != null) {
                        val filePart = createFilePart(context, imageUri)
                        if (filePart == null) {
                            return@withContext Result.failure(
                                Exception("Unable to read selected image")
                            )
                        }
                        authRepository.updateProfileWithImage(
                            file = filePart,
                            password = textPart(uiState.value.password),
                            name = textPart(uiState.value.name),
                            lastName = textPart(uiState.value.lastName),
                            sportType = textPart(uiState.value.sportType),
                            description = textPart(uiState.value.description),
                            weeklyGoal = weeklyGoal?.let { goal -> textPart(goal.toString()) }
                        )
                    } else {
                        val request =
                            UpdateUserProfileRequest(
                                password = uiState.value.password.trim().ifBlank { null },
                                name = uiState.value.name.trim().ifBlank { null },
                                lastName = uiState.value.lastName.trim().ifBlank { null },
                                picture = uiState.value.picture.trim().ifBlank { null },
                                sportType = uiState.value.sportType.trim().ifBlank { null },
                                preferences =
                                    weeklyGoal?.let { goal ->
                                        UserPreferencesDto(weeklyGoal = goal)
                                    },
                                description = uiState.value.description.trim().ifBlank { null }
                            )
                        authRepository.updateProfile(request)
                    }
                }

            result.fold(
                onSuccess = { profile ->
                    withContext(Dispatchers.IO) {
                        val entity = profile.toUserEntity()
                        AppDatabase.getInstance(context).userDao().upsert(entity)
                        UserSession.setUser(
                            userId = profile.extractId(),
                            name = profile.fullName(),
                            username = entity.username,
                            email = profile.email,
                            picture = profile.picture,
                            bio = profile.description,
                            streak = profile.streak
                        )
                    }
                    updateState { it.copy(isLoading = false, errorMessage = null) }
                    onSuccess()
                },
                onFailure = { error ->
                    updateState { it.copy(isLoading = false, errorMessage = error.message) }
                }
            )
        }
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
            val name = queryFileName(context, uri) ?: "avatar.jpg"
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            val body = bytes.toRequestBody(mimeType.toMediaType())
            MultipartBody.Part.createFormData("file", name, body)
        } catch (_: Exception) {
            null
        }
    }

    private fun queryFileName(context: Context, uri: Uri): String? {
        val resolver = context.contentResolver
        val cursor =
            resolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                ?: return null
        cursor.use {
            return if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index >= 0) it.getString(index) else null
            } else {
                null
            }
        }
    }
}
