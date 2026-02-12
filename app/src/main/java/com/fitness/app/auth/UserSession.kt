package com.fitness.app.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserSession {
    private const val PREFS_NAME = "fittrack_session"
    private const val KEY_ACCESS_TOKEN = "access_token"

    private val _name = MutableStateFlow<String?>(null)
    val name: StateFlow<String?> = _name.asStateFlow()

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username.asStateFlow()

    private val _email = MutableStateFlow<String?>(null)
    val email: StateFlow<String?> = _email.asStateFlow()

    private val _picture = MutableStateFlow<String?>(null)
    val picture: StateFlow<String?> = _picture.asStateFlow()

    private val _bio = MutableStateFlow<String?>(null)
    val bio: StateFlow<String?> = _bio.asStateFlow()

    private val _sportType = MutableStateFlow<String?>(null)
    val sportType: StateFlow<String?> = _sportType.asStateFlow()

    private val _streak = MutableStateFlow<Int?>(null)
    val streak: StateFlow<Int?> = _streak.asStateFlow()

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _refreshToken = MutableStateFlow<String?>(null)
    val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()

    private val _forcedLogoutVersion = MutableStateFlow(0L)
    val forcedLogoutVersion: StateFlow<Long> = _forcedLogoutVersion.asStateFlow()

    @Volatile private var accessTokenValue: String? = null
    @Volatile private var refreshTokenValue: String? = null

    fun persistAccessToken(context: android.content.Context, token: String?) {
        if (token.isNullOrBlank()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun restoreAccessToken(context: android.content.Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        if (!token.isNullOrBlank()) {
            setUser(accessToken = token)
        }
        return token
    }

    fun clearPersistedAccessToken(context: android.content.Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply()
    }

    fun setUser(
        name: String? = null,
        username: String? = null,
        email: String? = null,
        picture: String? = null,
        bio: String? = null,
        sportType: String? = null,
        streak: Int? = null,
        userId: String? = null,
        accessToken: String? = null,
        refreshToken: String? = null
    ) {
        if (name != null) _name.value = name
        if (username != null) _username.value = username
        if (email != null) _email.value = email
        if (picture != null) _picture.value = picture
        if (bio != null) _bio.value = bio
        if (sportType != null) _sportType.value = sportType
        if (streak != null) _streak.value = streak
        if (userId != null) _userId.value = userId
        if (accessToken != null) {
            _accessToken.value = accessToken
            accessTokenValue = accessToken
            _forcedLogoutVersion.value = 0L
        }
        if (refreshToken != null) {
            _refreshToken.value = refreshToken
            refreshTokenValue = refreshToken
            _forcedLogoutVersion.value = 0L
        }
    }

    fun clear() {
        _name.value = null
        _username.value = null
        _email.value = null
        _picture.value = null
        _bio.value = null
        _sportType.value = null
        _streak.value = null
        _userId.value = null
        _accessToken.value = null
        _refreshToken.value = null
        accessTokenValue = null
        refreshTokenValue = null
    }

    fun getAccessToken(): String? = accessTokenValue
    fun getRefreshToken(): String? = refreshTokenValue

    fun notifyForcedLogout() {
        _forcedLogoutVersion.value = _forcedLogoutVersion.value + 1L
    }
}
