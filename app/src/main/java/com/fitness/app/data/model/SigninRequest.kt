package com.fitness.app.data.model

import com.google.gson.annotations.SerializedName

data class SigninRequest(
    val username: String,
    val password: String,
    val name: String,
    @SerializedName("lastName") val lastName: String,
    val email: String
)
