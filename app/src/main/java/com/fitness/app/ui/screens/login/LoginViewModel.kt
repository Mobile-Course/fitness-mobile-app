package com.fitness.app.ui.screens.login

import androidx.lifecycle.viewModelScope
import com.fitness.app.network.ApiClient
import com.fitness.app.network.models.LoginRequest
import com.fitness.app.network.models.LoginResponse
import com.fitness.app.ui.base.BaseViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

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
    private val gson = Gson()

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
            updateState { it.copy(isLoading = true, errorMessage = null) }
            viewModelScope.launch {
                val result = withContext(Dispatchers.IO) {
                    try {
                        val body = gson.toJson(LoginRequest(uiState.value.email, uiState.value.password))
                        val response = ApiClient.post("/api/auth/login", body)
                        val responseBody = response.body?.string()
                        if (response.code == 401) {
                            return@withContext Result.failure(Exception("Unauthorized: invalid credentials"))
                        }
                        if (!response.isSuccessful) {
                            val message = responseBody?.takeIf { it.isNotBlank() } ?: "Login failed"
                            return@withContext Result.failure(Exception(message))
                        }
                        val parsed = if (!responseBody.isNullOrBlank()) {
                            gson.fromJson(responseBody, LoginResponse::class.java)
                        } else {
                            LoginResponse()
                        }
                        Result.success(parsed)
                    } catch (e: IOException) {
                        val msg = e.message?.takeIf { it.isNotBlank() } ?: "Network error. Please try again."
                        Result.failure(Exception(msg))
                    } catch (e: Exception) {
                        Result.failure(Exception("Unexpected error. Please try again."))
                    }
                }

                result.fold(
                    onSuccess = {
                        updateState { it.copy(isLoading = false) }
                        onSuccess()
                    },
                    onFailure = { error ->
                        updateState { it.copy(isLoading = false, errorMessage = error.message) }
                    }
                )
            }
        }
    }

    fun onGoogleCodeReceived(code: String, onSuccess: () -> Unit) {
        // TODO: Exchange code with backend for app session.
        updateState { it.copy(isLoading = true) }
        onSuccess()
    }

    fun clearError() {
        updateState { it.copy(errorMessage = null) }
    }
}
