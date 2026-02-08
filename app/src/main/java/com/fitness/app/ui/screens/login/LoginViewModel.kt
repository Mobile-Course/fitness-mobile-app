package com.fitness.app.ui.screens.login

import com.fitness.app.ui.base.BaseViewModel

data class LoginUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false
)

class LoginViewModel : BaseViewModel<LoginUiState>(LoginUiState()) {

    fun onEmailChanged(email: String) {
        updateState { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        updateState { it.copy(password = password, passwordError = null) }
    }

    fun togglePasswordVisibility() {
        updateState { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onSignInClicked(onSuccess: () -> Unit) {
        // Simple demo validation
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
            updateState { it.copy(isLoading = true) }
            // Simulate network call
            onSuccess()
        }
    }

    fun onGoogleCodeReceived(code: String, onSuccess: () -> Unit) {
        // TODO: Exchange code with backend for app session.
        updateState { it.copy(isLoading = true) }
        onSuccess()
    }
}
