package com.fitness.app.ui.screens.login

import androidx.lifecycle.viewModelScope
import com.fitness.app.data.repository.AuthRepository
import com.fitness.app.ui.base.BaseViewModel
import kotlinx.coroutines.launch

data class LoginUiState(
        val email: String = "",
        val emailError: String? = null,
        val password: String = "",
        val passwordError: String? = null,
        val isPasswordVisible: Boolean = false,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
)

class LoginViewModel : BaseViewModel<LoginUiState>(LoginUiState()) {
    private val authRepository = AuthRepository()

    fun onEmailChanged(email: String) {
        updateState { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        updateState { it.copy(password = password, passwordError = null, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        updateState { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onSignInClicked(onSuccess: () -> Unit) {
        // Validation
        var hasError = false
        if (uiState.value.email.isBlank()) {
            updateState { it.copy(emailError = "Email is required") }
            hasError = true
        }
        if (uiState.value.password.isBlank()) {
            updateState { it.copy(passwordError = "Password is required") }
            hasError = true
        }

        if (!hasError) {
            updateState { it.copy(isLoading = true, errorMessage = null) }

            viewModelScope.launch {
                val result =
                        authRepository.login(
                                email = uiState.value.email,
                                password = uiState.value.password
                        )

                result.fold(
                        onSuccess = { response ->
                            updateState { it.copy(isLoading = false) }
                            // Successfully logged in
                            onSuccess()
                        },
                        onFailure = { error ->
                            updateState {
                                it.copy(
                                        isLoading = false,
                                        errorMessage = error.message
                                                        ?: "Login failed. Please try again."
                                )
                            }
                        }
                )
            }
        }
    }
}
