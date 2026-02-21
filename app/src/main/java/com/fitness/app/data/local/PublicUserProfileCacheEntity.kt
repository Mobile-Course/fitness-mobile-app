package com.fitness.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "public_user_profiles_cache")
data class PublicUserProfileCacheEntity(
    @PrimaryKey val userId: String,
    val payloadJson: String,
    val updatedAt: Long
)
