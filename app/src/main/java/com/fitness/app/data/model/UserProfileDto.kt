package com.fitness.app.data.model

import com.google.gson.annotations.SerializedName
import com.fitness.app.data.local.UserEntity

data class UserProfileDto(
    @SerializedName("_id") val id: Any? = null,
    val username: String? = null,
    val name: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    val picture: String? = null,
    val email: String? = null,
    val streak: Int? = null,
    @SerializedName("sportType") val sportType: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("preferences") val preferences: UserPreferencesDto? = null
)

fun UserProfileDto.extractId(): String? {
    return when (id) {
        is String -> id
        is Map<*, *> -> id["\$oid"] as? String
        else -> null
    }
}

fun UserProfileDto.toUserEntity(): UserEntity {
    val key =
        listOfNotNull(username, email)
            .firstOrNull { it.isNotBlank() }
            ?: "user"
    return UserEntity(
        username = key,
        userId = extractId(),
        name = name,
        lastName = lastName,
        picture = picture,
        email = email,
        streak = streak,
        sportType = sportType,
        description = description
    )
}

fun UserProfileDto.fullName(): String? {
    return listOfNotNull(name, lastName)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" ")
        .ifBlank { null }
}
