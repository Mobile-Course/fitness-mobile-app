package com.fitness.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val username: String,
    val userId: String?,
    val refreshToken: String?,
    val name: String?,
    val lastName: String?,
    val picture: String?,
    val email: String?,
    val streak: Int?,
    val sportType: String?,
    val description: String?,
    val profileSummaryText: String? = null,
    val lastWorkoutVolume: Int? = null,
    val lastWorkoutIntensity: String? = null,
    val lastWorkoutFocusPoints: String? = null,
    val lastWorkoutCaloriesBurned: Int? = null,
    val lastWorkoutDuration: Int? = null,
    val updateCount: Int? = null,
    val currentWeight: Int? = null,
    val age: Int? = null,
    val sex: String? = null,
    val bodyFatPercentage: Int? = null,
    val oneRmSquat: Int? = null,
    val oneRmBench: Int? = null,
    val oneRmDeadlift: Int? = null,
    val workoutsPerWeek: Int? = null,
    val height: Int? = null,
    val vo2max: Int? = null
)
