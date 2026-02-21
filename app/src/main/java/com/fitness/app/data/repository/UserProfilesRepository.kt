package com.fitness.app.data.repository

import com.fitness.app.FitnessApp
import com.fitness.app.data.api.RetrofitClient
import com.fitness.app.data.local.AppDatabase
import com.fitness.app.data.local.PublicUserProfileCacheEntity
import com.fitness.app.data.model.PublicUserProfileDto
import com.fitness.app.data.model.UserProfileSummaryRequest
import com.fitness.app.data.model.UserProfileSummaryDto
import com.fitness.app.data.model.UserProfileXpStatsDto
import com.google.gson.Gson

class UserProfilesRepository {
    private val apiService = RetrofitClient.userProfilesApiService
    private val db by lazy { AppDatabase.getInstance(FitnessApp.instance) }
    private val gson = Gson()

    suspend fun getUserProfile(userId: String): Result<UserProfileSummaryDto> {
        return try {
            val response = apiService.getPublicProfile(userId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                cachePublicProfile(body, userId)
                Result.success(body.toUserProfileSummaryDto())
            } else {
                getCachedUserProfile(userId)?.let { return Result.success(it.toUserProfileSummaryDto()) }
                val details = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                val suffix = details?.let { " - $it" } ?: ""
                Result.failure(Exception("User profile fetch failed: ${response.message()}$suffix"))
            }
        } catch (e: Exception) {
            val cached = getCachedUserProfile(userId)
            if (cached != null) Result.success(cached.toUserProfileSummaryDto()) else Result.failure(e)
        }
    }

    suspend fun getPublicUserProfile(userId: String): Result<PublicUserProfileDto> {
        return try {
            val response = apiService.getPublicProfile(userId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                cachePublicProfile(body, userId)
                Result.success(body)
            } else {
                getCachedUserProfile(userId)?.let { return Result.success(it) }
                val details = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                val suffix = details?.let { " - $it" } ?: ""
                Result.failure(Exception("Public profile fetch failed: ${response.message()}$suffix"))
            }
        } catch (e: Exception) {
            val cached = getCachedUserProfile(userId)
            if (cached != null) Result.success(cached) else Result.failure(e)
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

    private suspend fun cachePublicProfile(profile: PublicUserProfileDto, requestedUserId: String) {
        val key = profile.id?.takeIf { it.isNotBlank() } ?: requestedUserId
        db.publicUserProfileCacheDao().upsert(
            PublicUserProfileCacheEntity(
                userId = key,
                payloadJson = gson.toJson(profile),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private suspend fun getCachedUserProfile(userId: String): PublicUserProfileDto? {
        val cached = db.publicUserProfileCacheDao().getByUserId(userId) ?: return null
        return runCatching {
            gson.fromJson(cached.payloadJson, PublicUserProfileDto::class.java)
        }.getOrNull()
    }
}

private fun PublicUserProfileDto.toUserProfileSummaryDto(): UserProfileSummaryDto {
    val source = profile ?: UserProfileSummaryDto()
    return source.copy(
        userId = id ?: source.userId,
        achievements = achievements ?: source.achievements,
        xpStats =
            xpStats ?: source.xpStats ?: UserProfileXpStatsDto(totalXp = totalXp, level = level)
    )
}
