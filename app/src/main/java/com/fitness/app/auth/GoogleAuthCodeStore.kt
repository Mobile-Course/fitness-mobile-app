package com.fitness.app.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object GoogleAuthCodeStore {
    private val _code = MutableStateFlow<String?>(null)
    val code: StateFlow<String?> = _code.asStateFlow()

    fun setCode(value: String?) {
        _code.value = value
    }

    fun clear() {
        _code.value = null
    }
}
