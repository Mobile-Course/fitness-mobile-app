package com.fitness.app.data.repository

import com.fitness.app.data.api.RetrofitClient
import com.fitness.app.data.model.PaginationResponse
import com.fitness.app.data.model.Post

class PostsRepository {
    private val apiService = RetrofitClient.postsApiService

    suspend fun getPosts(page: Int, limit: Int): Result<PaginationResponse<Post>> {
        return try {
            val response = apiService.getPosts(page, limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error fetching posts: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
