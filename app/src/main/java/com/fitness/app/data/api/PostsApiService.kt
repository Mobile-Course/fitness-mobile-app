package com.fitness.app.data.api

import com.fitness.app.data.model.PaginationResponse
import com.fitness.app.data.model.Post
import com.fitness.app.data.model.LikePostRequest
import com.fitness.app.data.model.AddCommentRequest
import com.fitness.app.data.model.CreatePostRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

interface PostsApiService {
    @GET("api/posts")
    suspend fun getPosts(
            @Query("page") page: Int,
            @Query("limit") limit: Int
    ): Response<PaginationResponse<Post>>

    @GET("api/posts/author/{authorId}")
    suspend fun getPostsByAuthor(
            @Path("authorId") authorId: String,
            @Query("page") page: Int,
            @Query("limit") limit: Int
    ): Response<PaginationResponse<Post>>

    @GET("api/posts/author/{authorId}")
    suspend fun getAllPostsByAuthor(
            @Path("authorId") authorId: String
    ): Response<PaginationResponse<Post>>

    @PUT("api/posts/like")
    suspend fun likeOrUnlikePost(@Body body: LikePostRequest): Response<Post>

    @POST("api/posts/{id}/comments")
    suspend fun addComment(
            @Path("id") postId: String,
            @Body body: AddCommentRequest
    ): Response<Post>

    @POST("api/posts")
    suspend fun createPostJson(
            @Body body: CreatePostRequest
    ): Response<Post>

    @Multipart
    @POST("api/posts")
    suspend fun createPostMultipart(
            @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
            @Part file: MultipartBody.Part? = null
    ): Response<Post>

    @retrofit2.http.DELETE("api/posts/{id}")
    suspend fun deletePost(@Path("id") postId: String): Response<Unit>

    @GET("api/posts/{id}")
    suspend fun getPost(@Path("id") id: String): Response<Post>

    @PUT("api/posts/{id}")
    suspend fun updatePost(@Path("id") id: String, @Body body: CreatePostRequest): Response<Post>

    @Multipart
    @PUT("api/posts/{id}")
    suspend fun updatePostMultipart(
            @Path("id") id: String,
            @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
            @Part file: MultipartBody.Part? = null
    ): Response<Post>
}
