package com.fitness.app.ui.screens.main

import androidx.lifecycle.viewModelScope
import com.fitness.app.auth.UserSession
import com.fitness.app.network.NetworkConfig
import com.fitness.app.ui.base.BaseViewModel
import com.fitness.app.utils.DataInvalidator
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Request

data class MainUiState(
    val forcedLogoutVersion: Long = 0L
)

data class AchievementShareDraft(
    val achievementName: String,
    val tier: String,
    val iconUrl: String,
    val postTitle: String,
    val postDescription: String
)

data class MainSnackbarEvent(
    val message: String,
    val actionLabel: String? = null,
    val achievementShareDraft: AchievementShareDraft? = null
)

class MainViewModel : BaseViewModel<MainUiState>(MainUiState()) {
    private val _snackbarEvents = MutableSharedFlow<MainSnackbarEvent>(extraBufferCapacity = 16)
    val snackbarEvents: SharedFlow<MainSnackbarEvent> = _snackbarEvents.asSharedFlow()

    private var streamJob: Job? = null
    private var streamCall: Call? = null
    private var lastKnownLevel: Int? = null

    init {
        viewModelScope.launch {
            UserSession.forcedLogoutVersion.collect { version ->
                updateState { it.copy(forcedLogoutVersion = version) }
            }
        }

        viewModelScope.launch {
            UserSession.userId.collect { userId ->
                if (userId.isNullOrBlank()) {
                    stopStream()
                } else {
                    lastKnownLevel = UserSession.level.value
                    startStream(userId)
                }
            }
        }
    }

    private fun startStream(userId: String) {
        if (streamJob?.isActive == true) return
        streamJob =
            viewModelScope.launch(Dispatchers.IO) {
                while (isActive && UserSession.userId.value == userId) {
                    val request =
                        Request.Builder()
                            .url("${NetworkConfig.BASE_URL}/api/notifications/stream")
                            .header("Accept", "text/event-stream")
                            .header("Cache-Control", "no-cache")
                            .build()
                    val call = NetworkConfig.okHttpClient.newCall(request)
                    streamCall = call

                    try {
                        call.execute().use { response ->
                            if (!response.isSuccessful || response.body == null) {
                                delay(2500)
                                return@use
                            }

                            val source = response.body!!.source()
                            val eventBuilder = StringBuilder()

                            while (isActive && !source.exhausted()) {
                                val line = source.readUtf8Line() ?: break
                                if (line.startsWith("data:")) {
                                    val payload = line.removePrefix("data:").trim()
                                    if (payload.isNotBlank()) {
                                        if (eventBuilder.isNotEmpty()) eventBuilder.append('\n')
                                        eventBuilder.append(payload)
                                    }
                                } else if (line.isBlank()) {
                                    if (eventBuilder.isNotEmpty()) {
                                        handleNotificationEvent(eventBuilder.toString())
                                        eventBuilder.clear()
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {
                        // Keep retry loop alive while session is active.
                    } finally {
                        streamCall = null
                    }

                    delay(2500)
                }
            }
    }

    private fun stopStream() {
        streamCall?.cancel()
        streamCall = null
        streamJob?.cancel()
        streamJob = null
    }

    private suspend fun handleNotificationEvent(rawPayload: String) {
        try {
            val parsedNotification = parseNotificationPayload(rawPayload) ?: return
            val type = parsedNotification.first
            val data = parsedNotification.second

            when (type) {
                "achievement_unlocked" -> {
                    val achievementName = data?.optString("achievementName")
                    val tier = data?.optString("tier")
                    val title = data?.optString("title")
                    val message = data?.optString("message")
                    val msg =
                        message.takeUnless { it.isNullOrBlank() }
                            ?: buildString {
                                append(title?.takeIf { it.isNotBlank() } ?: "Achievement unlocked")
                                if (!achievementName.isNullOrBlank()) {
                                    append(": ")
                                    append(achievementName)
                                }
                                if (!tier.isNullOrBlank()) {
                                    append(" (")
                                    append(tier.replaceFirstChar { it.uppercase() })
                                    append(")")
                                }
                            }
                    val shareDraft =
                        if (!achievementName.isNullOrBlank()) {
                            buildAchievementShareDraft(
                                achievementName = achievementName,
                                tier = tier ?: "bronze"
                            )
                        } else {
                            null
                        }

                    _snackbarEvents.emit(
                        MainSnackbarEvent(
                            message = msg,
                            actionLabel = if (shareDraft != null) "Share" else null,
                            achievementShareDraft = shareDraft
                        )
                    )
                    DataInvalidator.refreshProfile.value = true
                }
                "xp_earned" -> {
                    val xp = data?.optInt("xp")
                    val totalXp = data?.optInt("totalXp")
                    val level = data?.optInt("level")

                    UserSession.setUser(
                        totalXp = totalXp,
                        level = level
                    )

                    if (xp != null && xp > 0) {
                        val xpMessage =
                            if (totalXp != null) "+$xp XP earned (Total: $totalXp)"
                            else "+$xp XP earned"
                        _snackbarEvents.emit(MainSnackbarEvent(message = xpMessage))
                    }

                    if (level != null && (lastKnownLevel == null || level > (lastKnownLevel ?: 0))) {
                        _snackbarEvents.emit(
                            MainSnackbarEvent(message = "Level up! You reached level $level")
                        )
                    }
                    if (level != null) lastKnownLevel = level

                    DataInvalidator.refreshProfile.value = true
                }
                "system" -> {
                    val systemMsg =
                        data?.optString("message").takeUnless { it.isNullOrBlank() }
                            ?: "New notification"
                    _snackbarEvents.emit(MainSnackbarEvent(message = systemMsg))
                }
            }
        } catch (_: Exception) {
            // Ignore malformed SSE events.
        }
    }

    private fun parseNotificationPayload(rawPayload: String): Pair<String, JsonObject?>? {
        var element =
            runCatching { JsonParser.parseString(rawPayload) }.getOrNull()
                ?: return null

        repeat(4) {
            val obj = element.takeIf { it.isJsonObject }?.asJsonObject
            if (obj != null) {
                val type = obj.optString("type")
                if (!type.isNullOrBlank()) {
                    val data = obj.optObject("data") ?: parseObjectFromString(obj.optString("data"))
                    return type to data
                }

                val nested = obj.get("data")
                if (nested != null && !nested.isJsonNull) {
                    if (nested.isJsonPrimitive) {
                        val parsed = parseElementFromString(nested.asString) ?: return@repeat
                        element = parsed
                    } else {
                        element = nested
                    }
                    return@repeat
                }
            }

            if (element.isJsonPrimitive) {
                element = parseElementFromString(element.asString) ?: return null
                return@repeat
            }

            return null
        }

        return null
    }

    private fun parseElementFromString(raw: String?): com.google.gson.JsonElement? {
        val content = raw?.trim().takeUnless { it.isNullOrBlank() } ?: return null
        if (!(content.startsWith("{") || content.startsWith("["))) return null
        return runCatching { JsonParser.parseString(content) }.getOrNull()
    }

    private fun parseObjectFromString(raw: String?): JsonObject? {
        val parsed = parseElementFromString(raw) ?: return null
        return parsed.takeIf { it.isJsonObject }?.asJsonObject
    }

    private fun buildAchievementShareDraft(
        achievementName: String,
        tier: String
    ): AchievementShareDraft {
        val normalizedTier = tier.lowercase()
        val tierTitle = normalizedTier.replaceFirstChar { it.uppercase() }
        val iconPath =
            when (achievementName) {
                "First Steps" -> "/first-steps.png"
                "Workout Streak" -> "/workout-streak.png"
                "Volume King" -> "/volume-king.png"
                "Pain Free" -> "/pain-free.png"
                "Early Bird" -> "/early-bird.png"
                "AI Focused" -> "/ai-focused.png"
                "Consistency Master" -> "/consistency-master.png"
                else -> "/first-steps.png"
            }
        val resolvedIconUrl =
            if (iconPath.startsWith("http")) iconPath else "${NetworkConfig.BASE_URL}$iconPath"

        return AchievementShareDraft(
            achievementName = achievementName,
            tier = normalizedTier,
            iconUrl = resolvedIconUrl,
            postTitle = "$tierTitle $achievementName unlocked",
            postDescription = "I just unlocked $tierTitle tier in $achievementName. Staying consistent and pushing forward."
        )
    }

    override fun onCleared() {
        stopStream()
        super.onCleared()
    }
}

private fun JsonObject.optObject(name: String): JsonObject? {
    val el = this.get(name) ?: return null
    return if (el.isJsonObject) el.asJsonObject else null
}

private fun JsonObject.optString(name: String): String? {
    val el = this.get(name) ?: return null
    return if (el.isJsonNull) null else runCatching { el.asString }.getOrNull()
}

private fun JsonObject.optInt(name: String): Int? {
    val el = this.get(name) ?: return null
    return if (el.isJsonNull) null else runCatching { el.asInt }.getOrNull()
}
