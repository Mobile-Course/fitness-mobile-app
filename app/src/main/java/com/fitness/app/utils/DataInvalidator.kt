package com.fitness.app.utils

import kotlinx.coroutines.flow.MutableStateFlow

object DataInvalidator {
    val refreshFeed = MutableStateFlow(false)
    val refreshProfile = MutableStateFlow(false)
    val postUpdates = kotlinx.coroutines.flow.MutableSharedFlow<com.fitness.app.data.model.Post>(replay = 0, extraBufferCapacity = 10)
    val postDeletions = kotlinx.coroutines.flow.MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 10)
}
