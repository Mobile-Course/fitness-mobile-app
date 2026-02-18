package com.fitness.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fitness.app.data.model.Author
import com.fitness.app.data.model.Comment
import com.fitness.app.data.model.Like
import com.fitness.app.data.model.Post
import com.fitness.app.data.model.WorkoutDetails

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val src: String?,
    val pictures: List<String>?,
    val likes: List<Like>? = null, // Kept for model compatibility but nullified in DB
    val isLikedByMe: Boolean = false, // Normalized flag
    val likeNumber: Int,
    val commentsNumber: Int,
    val workoutDetails: WorkoutDetails?,
    val author: Author,
    val authorId: String,
    val comments: List<Comment>? = null, // Kept but nullified in DB
    val createdAt: String,
    val updatedAt: String,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toPost(): Post {
        return Post(
            id = id,
            title = title,
            description = description,
            src = src,
            pictures = pictures,
            likes = likes,
            isLikedByMe = isLikedByMe,
            likeNumber = likeNumber,
            commentsNumber = commentsNumber,
            workoutDetails = workoutDetails,
            author = author,
            comments = comments,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromPost(post: Post, currentUsername: String?): PostEntity {
            val likedByMe = currentUsername != null && post.likes?.any { it.username == currentUsername } == true
            return PostEntity(
                id = post.id,
                title = post.title,
                description = post.description,
                src = post.src,
                pictures = post.pictures,
                likes = null, // CRITICAL: Strip large JSON blobs to fix CursorWindow error
                isLikedByMe = likedByMe,
                likeNumber = post.likeNumber,
                commentsNumber = post.commentsNumber,
                workoutDetails = post.workoutDetails,
                author = post.author,
                authorId = post.author.id,
                comments = null, // CRITICAL: Strip large JSON blobs to fix CursorWindow error
                createdAt = post.createdAt,
                updatedAt = post.updatedAt,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
}
