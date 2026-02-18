package com.fitness.app.ui.screens.signup

import android.content.Context
import com.fitness.app.auth.UserSession
import com.fitness.app.data.model.extractId
import com.fitness.app.data.model.toUserEntity
import com.fitness.app.ui.base.BaseViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class SignupUiState(
    val fullName: String = "",
    val fullNameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false
)

class SignupViewModel : BaseViewModel<SignupUiState>(SignupUiState()) {
    private val authRepository = com.fitness.app.data.repository.AuthRepository()


    fun onFullNameChanged(name: String) {
        updateState { it.copy(fullName = name, fullNameError = null) }
    }

    fun onEmailChanged(email: String) {
        updateState { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        updateState { it.copy(password = password, passwordError = null) }
    }

    fun togglePasswordVisibility() {
        updateState { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onCreateAccountClicked(context: Context, onSuccess: () -> Unit) {
        var hasError = false
        if (uiState.value.fullName.isBlank()) {
            updateState { it.copy(fullNameError = "Full name is required") }
            hasError = true
        }
        if (uiState.value.email.isBlank()) {
            updateState { it.copy(emailError = "Email is required") }
            hasError = true
        }
        if (uiState.value.password.isBlank()) {
            updateState { it.copy(passwordError = "Password is required") }
            hasError = true
        }

        if (!hasError) {
            updateState { it.copy(isLoading = true) }
            
            viewModelScope.launch {
                val fullNameParts = uiState.value.fullName.trim().split(" ")
                val firstName = fullNameParts.firstOrNull() ?: ""
                var lastName = if (fullNameParts.size > 1) fullNameParts.drop(1).joinToString(" ") else "."
                if (lastName.isBlank()) lastName = "."
                
                // Use email as username to ensure consistency with Login screen which uses email
                // The previous 500 error was likely due to the empty lastName, which is now fixed.
                val username = uiState.value.email

                android.util.Log.d("SignupViewModel", "Attempting signup: username='$username', name='$firstName', lastName='$lastName', email='${uiState.value.email}'")

                val signupResult = authRepository.signup(
                    username = username,
                    password = uiState.value.password,
                    name = firstName,
                    lastName = lastName,
                    email = uiState.value.email
                )

                signupResult.fold(
                    onSuccess = {
                        android.util.Log.d("SignupViewModel", "Signup successful. Attempting auto-login.")
                        // Auto-login after successful signup
                        loginAfterSignup(context, onSuccess)
                    },
                    onFailure = { error ->
                        android.util.Log.e("SignupViewModel", "Signup failed", error)
                        updateState { it.copy(
                            isLoading = false,
                            emailError = error.message // Show error
                        ) }
                    }
                )
            }
        }
    }

    private fun loginAfterSignup(context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
             val loginResult = authRepository.login(uiState.value.email, uiState.value.password)
             loginResult.fold(
                 onSuccess = { response ->
                     // Persist user session
                     val user = response.user
                     if (user != null) {
                         UserSession.setUser(
                             accessToken = response.accessToken,
                             refreshToken = response.refreshToken,
                             userId = user.id?.toString(),
                             name = user.name,
                             username = user.username,
                             email = user.email,
                             picture = user.picture,
                             bio = user.description,
                             sportType = user.sportType,
                             streak = user.streak
                         )
                         
                         // Helper function to save to DB (similar to LoginViewModel)
                         saveUserToDb(context, response)
                         
                         updateState { it.copy(isLoading = false) }
                         onSuccess()
                     } else {
                         updateState { it.copy(isLoading = false, emailError = "Login succeeded but user data is missing.") }
                     }
                 },
                 onFailure = {
                     updateState { it.copy(isLoading = false, emailError = "Account created but login failed. Please login manually.") }
                 }
             )
        }
    }

    private fun saveUserToDb(context: Context, response: com.fitness.app.data.model.LoginResponse) {
        viewModelScope.launch(Dispatchers.IO) {
             try {
                val dao = com.fitness.app.data.local.AppDatabase.getInstance(context).userDao()
                
                // Fetch profile to get full details for persistence, similar to LoginViewModel
                val profileResult = authRepository.getProfile()
                val resolvedRefreshToken = response.refreshToken 
                    ?: com.fitness.app.network.NetworkConfig.getRefreshCookieValue() 

                val user = response.user

                val entity = if (profileResult.isSuccess) {
                    val profile = profileResult.getOrNull()!!
                    val entityFromProfile = profile.toUserEntity()
                    // Prioritize username from session/response
                    val finalUsername = user?.username ?: entityFromProfile.username
                    
                     entityFromProfile.copy(
                        username = finalUsername,
                        refreshToken = resolvedRefreshToken
                    )
                } else {
                    // Fallback if profile fetch fails
                    if (user != null) {
                         com.fitness.app.data.local.UserEntity(
                            username = user.username ?: user.email ?: "user",
                            userId = user.id?.toString(),
                            refreshToken = resolvedRefreshToken,
                            name = user.name,
                            lastName = null,
                            picture = user.picture,
                            email = user.email,
                            streak = user.streak,
                            sportType = user.sportType,
                            description = user.description
                        )
                    } else {
                         com.fitness.app.data.local.UserEntity(
                            username = uiState.value.email,
                            userId = null,
                            refreshToken = resolvedRefreshToken,
                            name = uiState.value.fullName,
                            lastName = null,
                            picture = null,
                            email = uiState.value.email,
                            streak = 0,
                            sportType = null,
                            description = null
                         )
                    }
                }
                
                dao.upsert(entity)
                
                // If we fetched profile successfully, update UserSession with full details
                if (profileResult.isSuccess) {
                    val profile = profileResult.getOrNull()!!
                    UserSession.setUser(
                        userId = profile.extractId(),
                        name = profile.name,
                        username = entity.username,
                        email = profile.email,
                        picture = profile.picture,
                        bio = profile.description,
                        sportType = profile.sportType,
                        streak = profile.streak,
                        refreshToken = resolvedRefreshToken
                    )
                }

             } catch (e: Exception) {
                 e.printStackTrace()
             }
        }
    }
}
