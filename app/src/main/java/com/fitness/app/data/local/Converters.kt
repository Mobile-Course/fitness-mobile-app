package com.fitness.app.data.local

import androidx.room.TypeConverter
import com.fitness.app.data.model.Author
import com.fitness.app.data.model.Comment
import com.fitness.app.data.model.Like
import com.fitness.app.data.model.WorkoutDetails
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String>? {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromLikeList(value: List<Like>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLikeList(value: String): List<Like>? {
        val type = object : TypeToken<List<Like>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromCommentList(value: List<Comment>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCommentList(value: String): List<Comment>? {
        val type = object : TypeToken<List<Comment>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromWorkoutDetails(value: WorkoutDetails?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toWorkoutDetails(value: String): WorkoutDetails? {
        return gson.fromJson(value, WorkoutDetails::class.java)
    }

    @TypeConverter
    fun fromAuthor(value: Author?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAuthor(value: String): Author? {
        return gson.fromJson(value, Author::class.java)
    }
}
