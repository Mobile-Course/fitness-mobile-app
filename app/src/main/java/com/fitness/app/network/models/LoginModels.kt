package com.fitness.app.network.models

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val access_token: String? = null,
    val refresh_token: String? = null,
    val user: User? = null
)

data class User(
    val name: String? = null,
    val lastName: String? = null,
    val username: String? = null,
    val picture: String? = null,
    val email: String? = null,
    val preferences: Any? = null
)
