package com.fitness.app.ui.screens.signup

import com.fitness.app.ui.base.BaseViewModel

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

    fun onCreateAccountClicked(onSuccess: () -> Unit) {
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
            // Simulate account creation
            onSuccess()
        }
    }
}
