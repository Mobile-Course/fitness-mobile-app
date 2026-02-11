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
    val description: String?,
    val profileSummaryText: String?,
    val lastWorkoutVolume: Int?,
    val lastWorkoutIntensity: String?,
    val lastWorkoutFocusPoints: String?,
    val lastWorkoutCaloriesBurned: Int?,
    val lastWorkoutDuration: Int?,
    val updateCount: Int?,
    val currentWeight: Int?,
    val age: Int?,
    val sex: String?,
    val bodyFatPercentage: Int?,
    val oneRmSquat: Int?,
    val oneRmBench: Int?,
    val oneRmDeadlift: Int?,
    val workoutsPerWeek: Int?,
    val height: Int?,
    val vo2max: Int?
)
