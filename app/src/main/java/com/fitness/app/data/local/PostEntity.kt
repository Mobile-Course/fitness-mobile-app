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
    val likes: List<Like>?,
    val likeNumber: Int,
    val commentsNumber: Int,
    val workoutDetails: WorkoutDetails?,
    val author: Author,
    val comments: List<Comment>?,
    val createdAt: String,
    val updatedAt: String,
    val lastUpdated: Long = System.currentTimeMillis() // For Delta Sync
) {
    fun toPost(): Post {
        return Post(
            id = id,
            title = title,
            description = description,
            src = src,
            pictures = pictures,
            likes = likes,
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
        fun fromPost(post: Post): PostEntity {
            return PostEntity(
                id = post.id,
                title = post.title,
                description = post.description,
                src = post.src,
                pictures = post.pictures,
                likes = post.likes,
                likeNumber = post.likeNumber,
                commentsNumber = post.commentsNumber,
                workoutDetails = post.workoutDetails,
                author = post.author,
                comments = post.comments,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
}
