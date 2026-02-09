package com.fitness.app.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserSession {
    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username.asStateFlow()

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    @Volatile private var accessTokenValue: String? = null

    fun setUser(username: String? = null, userId: String? = null, accessToken: String? = null) {
        if (username != null) _username.value = username
        if (userId != null) _userId.value = userId
        if (accessToken != null) {
            _accessToken.value = accessToken
            accessTokenValue = accessToken
        }
    }

    fun clear() {
        _username.value = null
        _userId.value = null
        _accessToken.value = null
        accessTokenValue = null
    }

    fun getAccessToken(): String? = accessTokenValue
}