package com.fitness.app.data.model

import com.google.gson.annotations.SerializedName


data class LoginResponse(
    @SerializedName("Authentication") val token: String?,
    @SerializedName("Refresh") val refreshToken: String?,
    val user: User?
) {
    val accessToken: String? get() = token
}

data class User(
    @SerializedName("_id") val id: String?,
    val username: String?,
    val email: String?,
    val name: String?,
    val picture: String? = null,
    val description: String? = null,
    val sportType: String? = null,
    val streak: Int? = 0
)
