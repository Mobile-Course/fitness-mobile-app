package com.fitness.app.data.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class AchievementDto(
    @SerializedName("_id") val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val category: String? = null,
    val type: String? = null,
    val tiers: List<AchievementTierDto>? = emptyList(),
    val icon: String? = null,
    val xpReward: Int? = null,
    val isActive: Boolean? = null
)

data class AchievementTierDto(
    val tier: String? = null,
    val threshold: Double? = null
)

data class UserAchievementDto(
    @SerializedName("_id") val id: String? = null,
    val userId: String? = null,
    val achievementId: JsonElement? = null,
    val currentTier: String? = null,
    val progressValue: Double? = null,
    val unlockedAt: String? = null,
    val history: List<AchievementHistoryDto>? = emptyList()
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

data class AchievementHistoryDto(
    val tier: String? = null,
    val unlockedAt: String? = null,
    val aiMessage: String? = null
)

data class AchievementXpDto(
    val xp: Int? = null,
    val totalXp: Int? = null,
    val level: Int? = null
)
