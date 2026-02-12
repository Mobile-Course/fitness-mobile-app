package com.fitness.app.data.model

import com.google.gson.annotations.SerializedName

data class UserPreferencesDto(
    @SerializedName("weeklyGoal") val weeklyGoal: Int? = null
)

data class UpdateUserProfileRequest(
    val username: String? = null,
    val email: String? = null,
    val picture: String? = null,
    val description: String? = null,
    @SerializedName("sportType") val sportType: String? = null
)
