package com.fitness.app.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DiscoverUserDto(
    @SerializedName("_id") val id: Any? = null,
    val username: String? = null,
    val name: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    val picture: String? = null,
    @SerializedName("sportType") val sportType: String? = null,
    @SerializedName("description") val description: String? = null
)

data class DiscoverUser(
    val id: String,
    val username: String,
    val name: String?,
    val lastName: String?,
    val picture: String?,
    val sportType: String?,
    val description: String?
) : Serializable {
    fun displayName(): String {
        val fullName =
            listOfNotNull(name, lastName)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .joinToString(" ")
        return fullName.ifBlank { username }
    }
}

fun DiscoverUserDto.toDiscoverUserOrNull(): DiscoverUser? {
    val userId =
        when (id) {
            is String -> id
            is Map<*, *> -> id["\$oid"] as? String
            else -> null
        }?.trim()
    val normalizedUsername = username?.trim().orEmpty()
    if (userId.isNullOrBlank() || normalizedUsername.isBlank()) return null

    return DiscoverUser(
        id = userId,
        username = normalizedUsername,
        name = name?.trim(),
        lastName = lastName?.trim(),
        picture = picture?.trim(),
        sportType = sportType?.trim(),
        description = description?.trim()
    )
}
