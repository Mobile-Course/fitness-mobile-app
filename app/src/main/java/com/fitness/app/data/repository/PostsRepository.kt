package com.fitness.app.data.repository

import com.fitness.app.data.api.RetrofitClient
import com.fitness.app.data.model.LikePostRequest
import com.fitness.app.data.model.AddCommentRequest
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

    suspend fun getPostsByAuthor(authorId: String, page: Int, limit: Int): Result<PaginationResponse<Post>> {
        return try {
            val response = apiService.getPostsByAuthor(authorId, page, limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error fetching posts: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun likeOrUnlikePost(postId: String): Result<Post> {
        return try {
            val response = apiService.likeOrUnlikePost(LikePostRequest(postId))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val message =
                        errorBody?.takeIf { it.isNotBlank() }
                                ?: response.message()
                                ?: "Unknown error"
                Result.failure(Exception("Error liking post: $message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(postId: String, content: String): Result<Post> {
        return try {
            val response = apiService.addComment(postId, AddCommentRequest(content))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val message =
                        errorBody?.takeIf { it.isNotBlank() }
                                ?: response.message()
                                ?: "Unknown error"
                Result.failure(Exception("Error adding comment: $message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
