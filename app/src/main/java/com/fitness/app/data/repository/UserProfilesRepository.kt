package com.fitness.app.data.repository

import com.fitness.app.data.api.RetrofitClient
import com.fitness.app.data.model.UserProfileSummaryRequest
import com.fitness.app.data.model.UserProfileSummaryDto

class UserProfilesRepository {
    private val apiService = RetrofitClient.userProfilesApiService

    suspend fun getUserProfile(userId: String): Result<UserProfileSummaryDto> {
        return try {
            val response = apiService.getUserProfile(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val details = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                val suffix = details?.let { " - $it" } ?: ""
                Result.failure(Exception("User profile fetch failed: ${response.message()}$suffix"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertUserProfile(request: UserProfileSummaryRequest): Result<Unit> {
        return try {
            val response = apiService.upsertUserProfile(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val details = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                val suffix = details?.let { " - $it" } ?: ""
                Result.failure(Exception("User profile update failed: ${response.message()}$suffix"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
