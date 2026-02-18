package com.fitness.app.ui.screens.main

import androidx.lifecycle.viewModelScope
import com.fitness.app.auth.UserSession
import com.fitness.app.ui.base.BaseViewModel
import kotlinx.coroutines.launch

data class MainUiState(
    val forcedLogoutVersion: Long = 0L
)

class MainViewModel : BaseViewModel<MainUiState>(MainUiState()) {
    init {
        viewModelScope.launch {
            UserSession.forcedLogoutVersion.collect { version ->
                updateState { it.copy(forcedLogoutVersion = version) }
            }
        }
    }
}
