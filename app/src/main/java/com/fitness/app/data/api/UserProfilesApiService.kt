package com.fitness.app.data.api

import com.fitness.app.data.model.UserProfileSummaryRequest
import com.fitness.app.data.model.UserProfileSummaryDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserProfilesApiService {
    @GET("api/user-profiles/{userId}")
    suspend fun getUserProfile(
        @Path("userId") userId: String
    ): Response<UserProfileSummaryDto>

    @POST("api/user-profiles")
    suspend fun upsertUserProfile(
        @Body request: UserProfileSummaryRequest
    ): Response<Unit>
}
