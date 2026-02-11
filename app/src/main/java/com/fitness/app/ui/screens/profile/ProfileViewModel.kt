package com.fitness.app.ui.screens.profile

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.fitness.app.auth.UserSession
import com.fitness.app.data.local.AppDatabase
import com.fitness.app.data.repository.AuthRepository
import com.fitness.app.network.NetworkConfig
import com.fitness.app.ui.base.BaseViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class UserProfile(
    val name: String = "User",
    val username: String = "user",
    val email: String = "",
    val picture: String? = null,
    val bio: String = "",
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
    private val authRepository = AuthRepository()

    init {
        viewModelScope.launch {
            combine(
                UserSession.name,
                UserSession.username,
                UserSession.email,
                UserSession.picture,
                UserSession.bio
            ) { name, username, email, picture, bio ->
                ProfileSessionFields(name, username, email, picture, bio)
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
                            picture = fields.picture ?: current.profile.picture,
                            bio =
                                fields.bio?.takeIf { it.isNotBlank() }
                                    ?: current.profile.bio
                        )
                    current.copy(profile = updatedProfile)
                }
            }
        }
    }

    fun logout(context: Context, onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                authRepository.logout()
            }
            withContext(Dispatchers.IO) {
                AppDatabase.getInstance(context).userDao().clear()
            }
            UserSession.clear()
            try {
                NetworkConfig.cookieManager.cookieStore.removeAll()
            } catch (_: Exception) {
            }
            onLoggedOut()
        }
    }
}

private data class ProfileSessionFields(
    val name: String?,
    val username: String?,
    val email: String?,
    val picture: String?,
    val bio: String?
)
