package com.fitness.app.data.model

import com.google.gson.annotations.SerializedName

data class LikePostRequest(
        @SerializedName("_id") val id: String
)

data class PaginationResponse<T>(
        val items: List<T>,
        val total: Int,
        val page: Int,
        val limit: Int,
        val totalPages: Int
)

data class Post(
        @SerializedName("_id") val id: String,
        val title: String,
        val description: String?,
        val pictures: List<String>?,
        val likes: List<Like>?,
        val likeNumber: Int,
        val workoutDetails: WorkoutDetails?,
        val author: Author,
        val comments: List<Comment>?,
        val createdAt: String,
        val updatedAt: String
)

data class Author(
        @SerializedName("_id") val id: String,
        val username: String,
        val picture: String?
)

data class Like(val username: String, val picture: String?)

data class WorkoutDetails(
        val type: String?,
        val duration: Int?,
        val calories: Int?,
        val subjectiveFeedbackFeelings: String?,
        val personalGoals: String?
)

data class Comment(val content: String, val author: Author, val createdAt: String)
