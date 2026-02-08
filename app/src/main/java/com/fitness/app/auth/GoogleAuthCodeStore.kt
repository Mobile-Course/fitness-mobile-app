package com.fitness.app.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GoogleAuthResult(
    val accessToken: String?,
    val refreshToken: String?,
    val userId: String?
)

object GoogleAuthCodeStore {
    private val _result = MutableStateFlow<GoogleAuthResult?>(null)
    val result: StateFlow<GoogleAuthResult?> = _result.asStateFlow()

    fun setResult(value: GoogleAuthResult?) {
        _result.value = value
    }

    fun clear() {
        _result.value = null
    }
}
