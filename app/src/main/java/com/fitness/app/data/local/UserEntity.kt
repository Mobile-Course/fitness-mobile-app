package com.fitness.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val username: String,
    val userId: String?,
    val name: String?,
    val lastName: String?,
    val picture: String?,
    val email: String?,
    val streak: Int?,
    val sportType: String?,
    val description: String?
)
