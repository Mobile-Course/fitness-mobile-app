package com.fitness.app.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserSession {
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

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    @Volatile private var accessTokenValue: String? = null

    fun setUser(
        name: String? = null,
        username: String? = null,
        email: String? = null,
        picture: String? = null,
        bio: String? = null,
        userId: String? = null,
        accessToken: String? = null
    ) {
        if (name != null) _name.value = name
        if (username != null) _username.value = username
        if (email != null) _email.value = email
        if (picture != null) _picture.value = picture
        if (bio != null) _bio.value = bio
        if (userId != null) _userId.value = userId
        if (accessToken != null) {
            _accessToken.value = accessToken
            accessTokenValue = accessToken
        }
    }

    fun clear() {
        _name.value = null
        _username.value = null
        _email.value = null
        _picture.value = null
        _bio.value = null
        _userId.value = null
        _accessToken.value = null
        accessTokenValue = null
    }

    fun getAccessToken(): String? = accessTokenValue
}
