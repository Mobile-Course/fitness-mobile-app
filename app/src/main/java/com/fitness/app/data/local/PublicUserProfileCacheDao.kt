package com.fitness.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PublicUserProfileCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PublicUserProfileCacheEntity)

    @Query("SELECT * FROM public_user_profiles_cache WHERE userId = :userId LIMIT 1")
    suspend fun getByUserId(userId: String): PublicUserProfileCacheEntity?
}
