package com.fitness.app.data.repository

import com.fitness.app.data.api.RetrofitClient
import com.fitness.app.data.model.LoginRequest
import com.fitness.app.data.model.LoginResponse
import com.fitness.app.data.model.UserProfileDto
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
                Result.failure(Exception("Profile update failed: ${response.message()}"))
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
