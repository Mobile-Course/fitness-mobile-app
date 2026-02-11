package com.fitness.app.data.model

data class UserProfileSummaryRequest(
    val userId: String,
    val profileSummaryText: String? = null,
    val profileSummaryJson: ProfileSummaryJson? = null,
    val currentWeight: Int? = null,
    val age: Int? = null,
    val sex: String? = null,
    val bodyFatPercentage: Int? = null,
    val oneRm: OneRm? = null,
    val workoutsPerWeek: Int? = null,
    val height: Int? = null,
    val vo2max: Int? = null
)

data class ProfileSummaryJson(
    val lastWorkout: LastWorkout? = null,
    val updateCount: Int? = null
)

data class LastWorkout(
    val volume: Int? = null,
    val intensity: String? = null,
    val focusPoints: List<String>? = null,
    val caloriesBurned: Int? = null,
    val duration: Int? = null
)

data class OneRm(
    val squat: Int? = null,
    val bench: Int? = null,
    val deadlift: Int? = null
)

data class UserProfileSummaryDto(
    @com.google.gson.annotations.SerializedName("_id")
    val id: String? = null,
    val userId: String? = null,
    @com.google.gson.annotations.SerializedName("__v")
    val version: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val profileSummaryText: String? = null,
    val profileSummaryJson: ProfileSummaryJson? = null,
    val currentWeight: Int? = null,
    val age: Int? = null,
    val sex: String? = null,
    val bodyFatPercentage: Int? = null,
    val oneRm: OneRm? = null,
    val workoutsPerWeek: Int? = null,
    val height: Int? = null,
    val vo2max: Int? = null
)
