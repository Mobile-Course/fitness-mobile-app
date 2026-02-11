package com.fitness.app.ui.screens.profile

import androidx.lifecycle.viewModelScope
import com.fitness.app.auth.UserSession
import com.fitness.app.ui.base.BaseViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class UserProfile(
    val name: String = "User",
    val username: String = "user",
    val email: String = "",
    val picture: String? = null,
    val bio: String = "Fitness enthusiast | Building strength",
    val workouts: Int = 0,
    val streak: Int = 0,
    val posts: Int = 0
)

data class Achievement(
    val title: String,
    val description: String,
    val icon: String // Using simplified icon representation for demo
)

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val achievements: List<Achievement> = listOf(
        Achievement("Early Bird", "Complete 10 morning workouts", "Trophy")
    ),
    val isLoading: Boolean = false
)

class ProfileViewModel : BaseViewModel<ProfileUiState>(ProfileUiState()) {
    init {
        viewModelScope.launch {
            combine(
                UserSession.name,
                UserSession.username,
                UserSession.email,
                UserSession.picture
            ) { name, username, email, picture ->
                ProfileSessionFields(name, username, email, picture)
            }.collect { fields ->
                updateState { current ->
                    val updatedProfile =
                        current.profile.copy(
                            name =
                                fields.name?.takeIf { it.isNotBlank() }
                                    ?: current.profile.name,
                            username =
                                fields.username?.takeIf { it.isNotBlank() }
                                    ?: current.profile.username,
                            email =
                                fields.email?.takeIf { it.isNotBlank() }
                                    ?: current.profile.email,
                            picture = fields.picture ?: current.profile.picture
                        )
                    current.copy(profile = updatedProfile)
                }
            }
        }
    }
}

private data class ProfileSessionFields(
    val name: String?,
    val username: String?,
    val email: String?,
    val picture: String?
)
