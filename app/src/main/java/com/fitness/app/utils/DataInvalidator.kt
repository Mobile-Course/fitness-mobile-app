package com.fitness.app.utils

import kotlinx.coroutines.flow.MutableStateFlow

object DataInvalidator {
    val refreshFeed = MutableStateFlow(false)
    val refreshProfile = MutableStateFlow(false)
}
