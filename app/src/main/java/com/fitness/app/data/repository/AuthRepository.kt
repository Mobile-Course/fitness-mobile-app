package com.fitness.app.data.repository

import com.fitness.app.data.api.RetrofitClient
import com.fitness.app.data.model.LoginRequest
import com.fitness.app.data.model.LoginResponse
import com.fitness.app.data.model.UpdateUserProfileRequest
import com.fitness.app.data.model.UserProfileDto
import com.fitness.app.data.model.DiscoverUser
import com.fitness.app.data.model.toDiscoverUserOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AuthRepository {
    private val apiService = RetrofitClient.authApiService

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(username = email, password = password))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signup(
        username: String,
        password: String,
        name: String,
        lastName: String,
        email: String
    ): Result<Unit> {
        return try {
            val response =
                apiService.signin(
                    com.fitness.app.data.model.SigninRequest(
                        username = username,
                        password = password,
                        name = name,
                        lastName = lastName,
                        email = email
                    )
                )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val error = response.errorBody()?.string()
                Result.failure(Exception("Signup failed: ${error ?: response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(): Result<UserProfileDto> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Profile fetch failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String): Result<List<DiscoverUser>> {
        return try {
            val response = apiService.searchUsers(query)
            if (response.isSuccessful && response.body() != null) {
                val users = response.body().orEmpty().mapNotNull { it.toDiscoverUserOrNull() }
                Result.success(users)
            } else {
                Result.failure(Exception("User search failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(request: UpdateUserProfileRequest): Result<UserProfileDto> {
        return try {
            val response = apiService.updateProfile(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val details = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                val suffix = details?.let { " - $it" } ?: ""
                Result.failure(Exception("Profile update failed: ${response.message()}$suffix"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfileWithImage(
        file: MultipartBody.Part? = null,
        password: RequestBody? = null,
        name: RequestBody? = null,
        lastName: RequestBody? = null,
        sportType: RequestBody? = null,
        description: RequestBody? = null,
        weeklyGoal: RequestBody? = null
    ): Result<UserProfileDto> {
        return try {
            val response =
                apiService.updateProfileWithImage(
                    file = file,
                    password = password,
                    name = name,
                    lastName = lastName,
                    sportType = sportType,
                    description = description,
                    weeklyGoal = weeklyGoal
                )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val details = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                val suffix = details?.let { " - $it" } ?: ""
                Result.failure(Exception("Profile update failed: ${response.message()}$suffix"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val response = apiService.logout()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Logout failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
