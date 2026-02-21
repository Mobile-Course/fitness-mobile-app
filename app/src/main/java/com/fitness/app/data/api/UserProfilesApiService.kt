package com.fitness.app.data.api

import com.fitness.app.data.model.UserProfileSummaryRequest
import com.fitness.app.data.model.UserProfileSummaryDto
import com.fitness.app.data.model.PublicUserProfileDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserProfilesApiService {
    @GET("api/auth/profile/{userId}")
    suspend fun getPublicProfile(
        @Path("userId") userId: String
    ): Response<PublicUserProfileDto>

    @POST("api/user-profiles")
    suspend fun upsertUserProfile(
        @Body request: UserProfileSummaryRequest
    ): Response<Unit>
}
