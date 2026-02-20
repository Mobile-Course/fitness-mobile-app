package com.fitness.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String,
    val type: String,
    val icon: String,
    val xpReward: Int,
    val isActive: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "user_achievements",
    primaryKeys = ["userId", "achievementId"]
)
data class UserAchievementEntity(
    val userId: String,
    val achievementId: String,
    val currentTier: String,
    val progressValue: Double,
    val unlockedAt: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_xp")
data class UserXpEntity(
    @PrimaryKey val userId: String,
    val xp: Int?,
    val totalXp: Int?,
    val level: Int?,
    val lastUpdated: Long = System.currentTimeMillis()
)
