package com.fitness.app.data.repository

import com.fitness.app.FitnessApp
import com.fitness.app.data.api.RetrofitClient
import com.fitness.app.data.local.AchievementEntity
import com.fitness.app.data.local.AppDatabase
import com.fitness.app.data.local.UserAchievementEntity
import com.fitness.app.data.local.UserXpEntity
import com.fitness.app.data.model.AchievementDto
import com.fitness.app.data.model.AchievementXpDto
import com.fitness.app.data.model.UserAchievementDto
import com.google.gson.JsonPrimitive

class AchievementsRepository {
    private val apiService = RetrofitClient.achievementsApiService
    private val db by lazy { AppDatabase.getInstance(FitnessApp.instance) }

    suspend fun getAllAchievements(): Result<List<AchievementDto>> {
        return try {
            val response = apiService.getAllAchievements()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body().orEmpty()
                db.achievementDao().clearAchievements()
                db.achievementDao().upsertAll(
                    body.mapNotNull { dto ->
                        val id = dto.id ?: return@mapNotNull null
                        AchievementEntity(
                            id = id,
                            name = dto.name.orEmpty(),
                            description = dto.description.orEmpty(),
                            category = dto.category.orEmpty(),
                            type = dto.type.orEmpty(),
                            icon = dto.icon.orEmpty(),
                            xpReward = dto.xpReward ?: 0,
                            isActive = dto.isActive ?: true
                        )
                    }
                )
                Result.success(body)
            } else {
                val cached = db.achievementDao().getActiveAchievementsSnapshot().map { it.toDto() }
                if (cached.isNotEmpty()) Result.success(cached)
                else Result.failure(Exception("Achievements fetch failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            val cached = db.achievementDao().getActiveAchievementsSnapshot().map { it.toDto() }
            if (cached.isNotEmpty()) Result.success(cached) else Result.failure(e)
        }
    }

    suspend fun getMyAchievements(): Result<List<UserAchievementDto>> {
        return try {
            val response = apiService.getMyAchievements()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body().orEmpty()
                val userId = resolveCurrentUserId()
                if (userId != null) {
                    db.achievementDao().clearUserAchievements(userId)
                    db.achievementDao().upsertAllUserAchievements(
                        body.mapNotNull { dto ->
                            val achievementId = dto.resolvedAchievementId() ?: return@mapNotNull null
                            UserAchievementEntity(
                                userId = userId,
                                achievementId = achievementId,
                                currentTier = dto.currentTier.orEmpty(),
                                progressValue = dto.progressValue ?: 0.0,
                                unlockedAt = dto.unlockedAt
                            )
                        }
                    )
                }
                Result.success(body)
            } else {
                val cached = getCachedUserAchievements()
                if (cached.isNotEmpty()) Result.success(cached)
                else Result.failure(Exception("User achievements fetch failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            val cached = getCachedUserAchievements()
            if (cached.isNotEmpty()) Result.success(cached) else Result.failure(e)
        }
    }

    suspend fun getMyXp(): Result<AchievementXpDto> {
        return try {
            val response = apiService.getMyXp()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val userId = resolveCurrentUserId()
                if (userId != null) {
                    db.achievementDao().upsertUserXp(
                        UserXpEntity(
                            userId = userId,
                            xp = body.xp,
                            totalXp = body.totalXp,
                            level = body.level
                        )
                    )
                }
                Result.success(body)
            } else {
                val cached = getCachedXp()
                if (cached != null) Result.success(cached)
                else Result.failure(Exception("XP fetch failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            val cached = getCachedXp()
            if (cached != null) Result.success(cached) else Result.failure(e)
        }
    }

    private suspend fun resolveCurrentUserId(): String? {
        return db.userDao().getUser()?.userId
    }

    private suspend fun getCachedUserAchievements(): List<UserAchievementDto> {
        val userId = resolveCurrentUserId() ?: return emptyList()
        return db.achievementDao().getUserAchievementsSnapshot(userId).map { it.toDto() }
    }

    private suspend fun getCachedXp(): AchievementXpDto? {
        val userId = resolveCurrentUserId() ?: return null
        return db.achievementDao().getUserXpSnapshot(userId)?.toDto()
    }
}

private fun AchievementEntity.toDto(): AchievementDto {
    return AchievementDto(
        id = id,
        name = name,
        description = description,
        category = category,
        type = type,
        tiers = emptyList(),
        icon = icon,
        xpReward = xpReward,
        isActive = isActive
    )
}

private fun UserAchievementEntity.toDto(): UserAchievementDto {
    return UserAchievementDto(
        id = null,
        userId = userId,
        achievementId = JsonPrimitive(achievementId),
        currentTier = currentTier,
        progressValue = progressValue,
        unlockedAt = unlockedAt,
        history = emptyList()
    )
}

private fun UserXpEntity.toDto(): AchievementXpDto {
    return AchievementXpDto(
        xp = xp,
        totalXp = totalXp,
        level = level
    )
}
