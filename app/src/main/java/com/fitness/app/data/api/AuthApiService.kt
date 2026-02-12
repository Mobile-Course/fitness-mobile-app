package com.fitness.app.data.api

import com.fitness.app.data.model.LoginRequest
import com.fitness.app.data.model.LoginResponse
import com.fitness.app.data.model.UpdateUserProfileRequest
import com.fitness.app.data.model.UserProfileDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApiService {
    @POST("api/auth/login") suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/auth/profile") suspend fun getProfile(): Response<UserProfileDto>

    @POST("api/auth/profile")
    suspend fun updateProfile(
        @Body request: UpdateUserProfileRequest
    ): Response<UserProfileDto>

    @Multipart
    @POST("api/auth/profile")
    suspend fun updateProfileWithImage(
        @Part file: MultipartBody.Part? = null,
        @Part("password") password: RequestBody? = null,
        @Part("name") name: RequestBody? = null,
        @Part("lastName") lastName: RequestBody? = null,
        @Part("sportType") sportType: RequestBody? = null,
        @Part("description") description: RequestBody? = null,
        @Part("preferences[weeklyGoal]") weeklyGoal: RequestBody? = null
    ): Response<UserProfileDto>

    @POST("api/auth/logout") suspend fun logout(): Response<Unit>
}
