package com.fitness.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(achievements: List<AchievementEntity>)

    @Query("SELECT * FROM achievements WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getActiveAchievementsSnapshot(): List<AchievementEntity>

    @Query("DELETE FROM achievements")
    suspend fun clearAchievements()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllUserAchievements(items: List<UserAchievementEntity>)

    @Query("SELECT * FROM user_achievements WHERE userId = :userId")
    suspend fun getUserAchievementsSnapshot(userId: String): List<UserAchievementEntity>

    @Query("DELETE FROM user_achievements WHERE userId = :userId")
    suspend fun clearUserAchievements(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserXp(userXp: UserXpEntity)

    @Query("SELECT * FROM user_xp WHERE userId = :userId LIMIT 1")
    suspend fun getUserXpSnapshot(userId: String): UserXpEntity?
}
