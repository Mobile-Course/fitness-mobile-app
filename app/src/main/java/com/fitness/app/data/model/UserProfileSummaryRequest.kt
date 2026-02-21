package com.fitness.app.data.model

data class UserProfileSummaryRequest(
    val userId: String,
    val profileSummaryJson: ProfileSummaryJson = ProfileSummaryJson(),
    val profileSummaryText: String = "",
    val version: Int? = 1,
    val currentWeight: Int? = null,
    val age: Int? = null,
    val sex: String? = null,
    val bodyFatPercentage: Int? = null,
    val oneRm: OneRm? = null,
    val workoutsPerWeek: Int? = null,
    val height: Int? = null,
    val vo2max: Int? = null
)

data class ProfileSummaryJson(
    val lastWorkout: LastWorkout? = null,
    val updateCount: Int? = null
)

data class LastWorkout(
    val volume: Int? = null,
    val intensity: String? = null,
    val focusPoints: List<String>? = null,
    val caloriesBurned: Int? = null,
    val duration: Int? = null
)

data class OneRm(
    val squat: Int? = null,
    val bench: Int? = null,
    val deadlift: Int? = null
)

data class UserProfileSummaryDto(
    @com.google.gson.annotations.SerializedName("_id")
    val id: String? = null,
    val userId: String? = null,
    @com.google.gson.annotations.SerializedName("__v")
    val version: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val profileSummaryText: String? = null,
    val profileSummaryJson: ProfileSummaryJson? = null,
    val currentWeight: Int? = null,
    val age: Int? = null,
    val sex: String? = null,
    val bodyFatPercentage: Int? = null,
    val oneRm: OneRm? = null,
    val workoutsPerWeek: Int? = null,
    val height: Int? = null,
    val vo2max: Int? = null,
    val achievements: List<UserProfileAchievementDto>? = null,
    val xpStats: UserProfileXpStatsDto? = null
)

data class UserProfileAchievementDto(
    @com.google.gson.annotations.SerializedName("_id")
    val id: String? = null,
    val achievementId: com.google.gson.JsonElement? = null,
    val name: String? = null,
    val description: String? = null,
    val icon: String? = null,
    val currentTier: String? = null
) {
    fun resolvedAchievementId(): String? {
        val raw = achievementId ?: return null
        return when {
            raw.isJsonNull -> null
            raw.isJsonPrimitive -> raw.asString
            raw.isJsonObject -> {
                val obj = raw.asJsonObject
                obj.get("_id")?.asString ?: obj.get("id")?.asString
            }
            else -> null
        }
    }
}

data class UserProfileXpStatsDto(
    val xp: Int? = null,
    val totalXp: Int? = null,
    val level: Int? = null
)

data class PublicUserProfileDto(
    @com.google.gson.annotations.SerializedName("_id")
    val id: String? = null,
    val name: String? = null,
    val lastName: String? = null,
    val username: String? = null,
    val picture: String? = null,
    val description: String? = null,
    val sportType: String? = null,
    val totalXp: Int? = null,
    val level: Int? = null,
    val streak: Int? = null,
    val postsCount: Int? = null,
    val profile: UserProfileSummaryDto? = null,
    val achievements: List<UserProfileAchievementDto>? = null,
    val xpStats: UserProfileXpStatsDto? = null
)
