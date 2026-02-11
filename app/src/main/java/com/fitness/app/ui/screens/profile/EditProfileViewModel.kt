package com.fitness.app.ui.screens.profile

import android.content.Context
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

data class EditProfileUiState(
    val password: String = "",
    val name: String = "",
    val lastName: String = "",
    val picture: String = "",
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
            val request =
                UpdateUserProfileRequest(
                    password = uiState.value.password.trim().ifBlank { null },
                    name = uiState.value.name.trim().ifBlank { null },
                    lastName = uiState.value.lastName.trim().ifBlank { null },
                    picture = uiState.value.picture.trim().ifBlank { null },
                    sportType = uiState.value.sportType.trim().ifBlank { null },
                    preferences =
                        weeklyGoal?.let { goal -> UserPreferencesDto(weeklyGoal = goal) },
                    description = uiState.value.description.trim().ifBlank { null }
                )

            val result =
                withContext(Dispatchers.IO) {
                    authRepository.updateProfile(request)
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
                            bio = profile.description
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
}
