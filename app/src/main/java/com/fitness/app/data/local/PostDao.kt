package com.fitness.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(posts: List<PostEntity>)

    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    suspend fun getAllPostsSnapshot(): List<PostEntity>

    @Query("SELECT * FROM posts ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagedPostsSnapshot(limit: Int, offset: Int): List<PostEntity>

    @Query("SELECT MAX(lastUpdated) FROM posts")
    suspend fun getLastUpdated(): Long?

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: String)

    @Query("DELETE FROM posts")
    suspend fun clear()
}
