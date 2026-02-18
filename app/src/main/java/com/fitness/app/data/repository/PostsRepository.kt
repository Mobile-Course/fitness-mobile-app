package com.fitness.app.data.repository

import com.fitness.app.data.api.RetrofitClient
import com.fitness.app.data.model.LikePostRequest
import com.fitness.app.data.model.AddCommentRequest
import com.fitness.app.data.model.CreatePostRequest
import com.fitness.app.data.model.PaginationResponse
import com.fitness.app.data.model.Post
import okhttp3.MultipartBody
import okhttp3.RequestBody

class PostsRepository {
    private val apiService = RetrofitClient.postsApiService

    suspend fun getPosts(page: Int, limit: Int): Result<PaginationResponse<Post>> {
        return try {
            val context = com.fitness.app.FitnessApp.instance
            val db = com.fitness.app.data.local.AppDatabase.getInstance(context)
            val dao = db.postDao()
            val userDao = db.userDao()
            val currentUsername = userDao.getUser()?.username

            // 1. Attempt to fetch from network first for the most accurate state
            try {
                val response = apiService.getPosts(page = page, limit = limit)
                if (response.isSuccessful && response.body() != null) {
                    val apiPosts = response.body()!!.items
                    
                    // If this is the first page and we are online, we should clear the cache 
                    // to remove any posts that might have been deleted on the server.
                    if (page == 1) {
                        dao.clear()
                    }
                    
                    if (apiPosts.isNotEmpty()) {
                        val entities = apiPosts.map { com.fitness.app.data.local.PostEntity.fromPost(it, currentUsername) }
                        dao.upsertAll(entities)
                    }
                    
                    return Result.success(response.body()!!)
                }
            } catch (e: Exception) {
                android.util.Log.e("PostsRepository", "Network fetch failed for page $page, falling back to cache", e)
            }

            // 2. Fallback to local database if network fails or returns error
            val offset = (page - 1) * limit
            val localPagedPosts = dao.getPagedPostsSnapshot(limit, offset)
            val mappedPosts = localPagedPosts.map { it.toPost() }
            
            val hasMore = mappedPosts.size == limit
            val estimatedTotal = if (hasMore) offset + limit + 1 else offset + mappedPosts.size

            Result.success(
                PaginationResponse(
                    items = mappedPosts,
                    total = estimatedTotal,
                    page = page,
                    limit = limit,
                    totalPages = (estimatedTotal + limit - 1) / limit
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPostsByAuthor(authorId: String, page: Int, limit: Int): Result<PaginationResponse<Post>> {
        return try {
            val context = com.fitness.app.FitnessApp.instance
            val db = com.fitness.app.data.local.AppDatabase.getInstance(context)
            val dao = db.postDao()
            val userDao = db.userDao()
            val currentUsername = userDao.getUser()?.username

            // 1. Attempt to fetch from network first
            try {
                val response = apiService.getPostsByAuthor(authorId, page, limit)
                if (response.isSuccessful && response.body() != null) {
                    val apiPosts = response.body()!!.items
                    
                    // Clear author-specific cache on page 1
                    if (page == 1) {
                        dao.clearByAuthor(authorId)
                    }
                    
                    if (apiPosts.isNotEmpty()) {
                        val entities = apiPosts.map { com.fitness.app.data.local.PostEntity.fromPost(it, currentUsername) }
                        dao.upsertAll(entities)
                    }
                    
                    return Result.success(response.body()!!)
                }
            } catch (e: Exception) {
                android.util.Log.e("PostsRepository", "Network fetch failed for author $authorId page $page", e)
            }

            // 2. Fallback to local database
            val offset = (page - 1) * limit
            val localPagedPosts = dao.getPagedPostsByAuthorSnapshot(authorId, limit, offset)
            val mappedPosts = localPagedPosts.map { it.toPost() }
            
            val hasMore = mappedPosts.size == limit
            val estimatedTotal = if (hasMore) offset + limit + 1 else offset + mappedPosts.size

            Result.success(
                PaginationResponse(
                    items = mappedPosts,
                    total = estimatedTotal,
                    page = page,
                    limit = limit,
                    totalPages = (estimatedTotal + limit - 1) / limit
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllPostsByAuthor(authorId: String): Result<PaginationResponse<Post>> {
        return try {
            val context = com.fitness.app.FitnessApp.instance
            val db = com.fitness.app.data.local.AppDatabase.getInstance(context)
            val dao = db.postDao()
            val userDao = db.userDao()
            val currentUsername = userDao.getUser()?.username

            // 1. Attempt network fetch
            try {
                val response = apiService.getAllPostsByAuthor(authorId)
                if (response.isSuccessful && response.body() != null) {
                    val apiPosts = response.body()!!.items
                    
                    // Clear author cache for "all posts" sync
                    dao.clearByAuthor(authorId)
                    
                    if (apiPosts.isNotEmpty()) {
                        val entities = apiPosts.map { com.fitness.app.data.local.PostEntity.fromPost(it, currentUsername) }
                        dao.upsertAll(entities)
                    }
                    return Result.success(response.body()!!)
                }
            } catch (e: Exception) {
                android.util.Log.e("PostsRepository", "Network fetch failed for all posts of author $authorId", e)
            }

            // 2. Fallback to local DB (no pagination needed here as it's "all")
            val localPosts = dao.getAllPostsSnapshot().filter { it.authorId == authorId }
            val mappedPosts = localPosts.map { it.toPost() }

            Result.success(
                PaginationResponse(
                    items = mappedPosts,
                    total = mappedPosts.size,
                    page = 1,
                    limit = mappedPosts.size.coerceAtLeast(1),
                    totalPages = 1
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun likeOrUnlikePost(postId: String): Result<Post> {
        return try {
            val response = apiService.likeOrUnlikePost(LikePostRequest(postId))
            if (response.isSuccessful && response.body() != null) {
                var updatedPost = response.body()!!
                
                // Compute isLikedByMe before broadcasting
                val context = com.fitness.app.FitnessApp.instance
                val db = com.fitness.app.data.local.AppDatabase.getInstance(context)
                val currentUsername = db.userDao().getUser()?.username
                
                val likedByMe = currentUsername != null && updatedPost.likes?.any { it.username == currentUsername } == true
                updatedPost = updatedPost.copy(isLikedByMe = likedByMe)
                
                // Update local DB for persistence
                db.postDao().upsertAll(listOf(com.fitness.app.data.local.PostEntity.fromPost(updatedPost, currentUsername)))
                
                com.fitness.app.utils.DataInvalidator.postUpdates.emit(updatedPost)
                Result.success(updatedPost)
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
                var updatedPost = response.body()!!
                
                // Compute isLikedByMe before broadcasting
                val context = com.fitness.app.FitnessApp.instance
                val db = com.fitness.app.data.local.AppDatabase.getInstance(context)
                val currentUsername = db.userDao().getUser()?.username
                
                val likedByMe = currentUsername != null && updatedPost.likes?.any { it.username == currentUsername } == true
                updatedPost = updatedPost.copy(isLikedByMe = likedByMe)
                
                // Update local DB for persistence
                db.postDao().upsertAll(listOf(com.fitness.app.data.local.PostEntity.fromPost(updatedPost, currentUsername)))
                
                com.fitness.app.utils.DataInvalidator.postUpdates.emit(updatedPost)
                Result.success(updatedPost)
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

    suspend fun createPost(
        body: CreatePostRequest
    ): Result<Post> {
        return try {
            val response = apiService.createPostJson(body)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val message =
                    errorBody?.takeIf { it.isNotBlank() }
                        ?: response.message()
                        ?: "Unknown error"
                Result.failure(Exception("Error creating post: $message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPostMultipart(
        fields: Map<String, RequestBody>,
        file: MultipartBody.Part? = null
    ): Result<Post> {
        return try {
            val response = apiService.createPostMultipart(fields = fields, file = file)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val message =
                    errorBody?.takeIf { it.isNotBlank() }
                        ?: response.message()
                        ?: "Unknown error"
                Result.failure(Exception("Error creating post: $message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            val response = apiService.deletePost(postId)
            if (response.isSuccessful) {
                // Update local DB for persistence
                val context = com.fitness.app.FitnessApp.instance
                val db = com.fitness.app.data.local.AppDatabase.getInstance(context)
                db.postDao().deletePost(postId)
                
                com.fitness.app.utils.DataInvalidator.postDeletions.emit(postId)
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val message =
                    errorBody?.takeIf { it.isNotBlank() }
                        ?: response.message()
                        ?: "Unknown error"
                Result.failure(Exception("Error deleting post: $message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPost(id: String): Result<Post> {
        return try {
            val response = apiService.getPost(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error fetching post"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePost(id: String, request: CreatePostRequest): Result<Post> {
        return try {
            val response = apiService.updatePost(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error updating post"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePostMultipart(id: String, fields: Map<String, RequestBody>, file: MultipartBody.Part?): Result<Post> {
        return try {
            val response = apiService.updatePostMultipart(id, fields, file)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val message =
                    errorBody?.takeIf { it.isNotBlank() }
                        ?: response.message()
                        ?: "Unknown error"
                Result.failure(Exception("Error updating post with image: $message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
