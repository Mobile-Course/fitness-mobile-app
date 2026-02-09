package com.fitness.app.data.api

import com.fitness.app.data.model.PaginationResponse
import com.fitness.app.data.model.Post
import com.fitness.app.data.model.LikePostRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface PostsApiService {
    @GET("api/posts")
    suspend fun getPosts(
            @Query("page") page: Int,
            @Query("limit") limit: Int
    ): Response<PaginationResponse<Post>>

    @PUT("api/posts/like")
    suspend fun likeOrUnlikePost(@Body body: LikePostRequest): Response<Post>
}
