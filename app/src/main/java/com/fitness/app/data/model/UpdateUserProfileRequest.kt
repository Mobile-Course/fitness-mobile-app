package com.fitness.app.data.model

import com.google.gson.annotations.SerializedName

data class UserPreferencesDto(
    @SerializedName("weeklyGoal") val weeklyGoal: Int? = null
)

data class UpdateUserProfileRequest(
    val password: String? = null,
    val name: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    val picture: String? = null,
    @SerializedName("sportType") val sportType: String? = null,
    val preferences: UserPreferencesDto? = null,
    val description: String? = null
)
