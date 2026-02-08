package com.fitness.app.data.model

data class LoginResponse(val token: String?, val user: User?)

data class User(val id: String, val username: String, val email: String)
