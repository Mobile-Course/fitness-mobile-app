package com.fitness.app.data.api

import com.fitness.app.data.model.LoginRequest
import com.fitness.app.data.model.LoginResponse
import com.fitness.app.data.model.UpdateUserProfileRequest
import com.fitness.app.data.model.UserProfileDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login") suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/auth/profile") suspend fun getProfile(): Response<UserProfileDto>

    @POST("api/auth/profile")
    suspend fun updateProfile(
        @Body request: UpdateUserProfileRequest
    ): Response<UserProfileDto>

    @POST("api/auth/logout") suspend fun logout(): Response<Unit>
}
