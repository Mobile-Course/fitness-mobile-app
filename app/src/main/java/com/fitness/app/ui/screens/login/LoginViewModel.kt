package com.fitness.app.ui.screens.login

import androidx.lifecycle.viewModelScope
import com.fitness.app.network.ApiClient
import com.fitness.app.network.models.LoginRequest
import com.google.gson.JsonObject
import com.fitness.app.auth.UserSession
import com.fitness.app.network.NetworkConfig
import com.fitness.app.ui.base.BaseViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import android.util.Base64
import org.json.JSONObject
import android.content.Context
import com.fitness.app.data.local.AppDatabase
import com.fitness.app.data.model.extractId
import com.fitness.app.data.model.fullName
import com.fitness.app.data.model.toUserEntity
import com.fitness.app.data.repository.AuthRepository
import com.fitness.app.data.repository.UserProfilesRepository

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
    private val authRepository = AuthRepository()
    private val userProfilesRepository = UserProfilesRepository()

    fun onEmailChanged(email: String) {
        updateState { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        updateState { it.copy(password = password, passwordError = null, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        updateState { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onSignInClicked(onSuccess: () -> Unit, context: Context) {
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
                val result = withContext(Dispatchers.IO) {
                    try {
                        val body = gson.toJson(LoginRequest(uiState.value.email, uiState.value.password))
                        val response = ApiClient.post("/api/auth/login", body)
                        val responseBody = response.body?.string()
                        val authHeader =
                            response.header("Authentication")
                                ?: response.header("Authorization")
                        val setCookieHeaders = response.headers("Set-Cookie")
                        val cookieAuth = setCookieHeaders
                            .firstOrNull { it.startsWith("Authentication=") }
                            ?.substringAfter("Authentication=")
                            ?.substringBefore(";")
                        android.util.Log.d(
                            "LoginViewModel",
                            "Login headers auth=${authHeader != null} setCookieCount=${setCookieHeaders.size}"
                        )
                        if (response.code == 401) {
                            return@withContext Result.failure(Exception("Unauthorized: invalid credentials"))
                        }
                        if (!response.isSuccessful) {
                            val message = responseBody?.takeIf { it.isNotBlank() } ?: "Login failed"
                            return@withContext Result.failure(Exception(message))
                        }
                        if (!responseBody.isNullOrBlank()) {
                            val obj = gson.fromJson(responseBody, JsonObject::class.java)
                            val token =
                                obj.get("Authentication")?.asString
                                    ?: obj.get("token")?.asString
                            val userObj = obj.getAsJsonObject("user")
                            val username =
                                userObj?.get("username")?.asString
                                    ?: userObj?.get("email")?.asString
                                    ?: uiState.value.email
                            val name = userObj?.get("name")?.asString
                            val lastName = userObj?.get("lastName")?.asString
                            val fullName =
                                listOfNotNull(name, lastName)
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                    .joinToString(" ")
                                    .ifBlank { null }
                            val email = userObj?.get("email")?.asString ?: uiState.value.email
                            val picture = userObj?.get("picture")?.asString
                            val description =
                                userObj?.get("description")?.asString
                            val sportType = userObj?.get("sportType")?.asString
                            val cookieToken = NetworkConfig.getAuthCookieValue()
                            val resolvedToken = token ?: authHeader ?: cookieAuth ?: cookieToken
                            UserSession.setUser(
                                name = fullName,
                                username = username,
                                email = email,
                                picture = picture,
                                bio = description,
                                sportType = sportType,
                                accessToken = resolvedToken
                            )
                            UserSession.persistAccessToken(context, resolvedToken)
                        } else {
                            UserSession.setUser(username = uiState.value.email, email = uiState.value.email)
                        }
                        android.util.Log.d(
                            "LoginViewModel",
                            "Cookies after login: ${NetworkConfig.dumpCookies()}"
                        )
                        fetchAndPersistProfile(context)
                        Result.success(Unit)
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

    fun onGoogleTokensReceived(
        accessToken: String?,
        refreshToken: String?,
        userId: String?,
        context: Context,
        onSuccess: () -> Unit
    ) {
        // TODO: Persist tokens securely and fetch user profile if needed.
        if (accessToken.isNullOrBlank()) {
            updateState { it.copy(errorMessage = "Google login failed: missing token") }
            return
        }
        android.util.Log.d(
            "LoginViewModel",
            "Google tokens received. accessTokenLength=${accessToken.length}"
        )
        val username = extractUsernameFromJwt(accessToken)
        val email = username?.takeIf { it.contains("@") }
        UserSession.setUser(
            userId = userId,
            username = username,
            email = email,
            accessToken = accessToken
        )
        UserSession.persistAccessToken(context, accessToken)
        viewModelScope.launch {
            fetchAndPersistProfile(context)
            updateState { it.copy(isLoading = false, errorMessage = null) }
            onSuccess()
        }
    }

    fun clearError() {
        updateState { it.copy(errorMessage = null) }
    }

    private fun extractUsernameFromJwt(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payloadJson =
                String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            val obj = JSONObject(payloadJson)
            obj.optString("username")
                .ifBlank {
                    obj.optString("email")
                }
                .ifBlank { null }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchAndPersistProfile(context: Context) {
        withContext(Dispatchers.IO) {
            val profileResult = authRepository.getProfile()
            profileResult.fold(
                onSuccess = { profile ->
                    val entity = profile.toUserEntity()
                    val dao = AppDatabase.getInstance(context).userDao()
                    dao.upsert(entity)
                    UserSession.setUser(
                        userId = profile.extractId(),
                        name = profile.fullName(),
                        username = entity.username,
                        email = profile.email,
                        picture = profile.picture,
                        bio = profile.description,
                        sportType = profile.sportType,
                        streak = profile.streak
                    )
                    val userId = profile.extractId()
                    if (!userId.isNullOrBlank()) {
                        val extraResult = userProfilesRepository.getUserProfile(userId)
                        extraResult.onSuccess { extra ->
                            val updated =
                                entity.copy(
                                    profileSummaryText = extra.profileSummaryText,
                                    lastWorkoutVolume = extra.profileSummaryJson?.lastWorkout?.volume,
                                    lastWorkoutIntensity =
                                        extra.profileSummaryJson?.lastWorkout?.intensity,
                                    lastWorkoutFocusPoints =
                                        extra.profileSummaryJson?.lastWorkout?.focusPoints
                                            ?.joinToString(","),
                                    lastWorkoutCaloriesBurned =
                                        extra.profileSummaryJson?.lastWorkout?.caloriesBurned,
                                    lastWorkoutDuration =
                                        extra.profileSummaryJson?.lastWorkout?.duration,
                                    updateCount = extra.profileSummaryJson?.updateCount,
                                    currentWeight = extra.currentWeight,
                                    age = extra.age,
                                    sex = extra.sex,
                                    bodyFatPercentage = extra.bodyFatPercentage,
                                    oneRmSquat = extra.oneRm?.squat,
                                    oneRmBench = extra.oneRm?.bench,
                                    oneRmDeadlift = extra.oneRm?.deadlift,
                                    workoutsPerWeek = extra.workoutsPerWeek,
                                    height = extra.height,
                                    vo2max = extra.vo2max
                                )
                            dao.upsert(updated)
                        }
                    }
                },
                onFailure = { error ->
                    android.util.Log.e(
                        "LoginViewModel",
                        "Profile fetch failed: ${error.message}",
                        error
                    )
                }
            )
        }
    }
}
