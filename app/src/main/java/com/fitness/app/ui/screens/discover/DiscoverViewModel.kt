package com.fitness.app.ui.screens.discover

import com.fitness.app.auth.UserSession
import com.fitness.app.data.model.DiscoverUser
import com.fitness.app.data.repository.AuthRepository
import com.fitness.app.ui.base.BaseViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class DiscoverUiState(
    val query: String = "",
    val users: List<DiscoverUser> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DiscoverViewModel : BaseViewModel<DiscoverUiState>(DiscoverUiState()) {
    private val authRepository = AuthRepository()
    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        updateState { it.copy(query = query, error = null) }
    }

    fun searchUsers() {
        val query = uiState.value.query.trim()
        if (query.isBlank()) {
            updateState { it.copy(users = emptyList(), isLoading = false, error = null) }
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            authRepository.searchUsers(query)
                .onSuccess { users ->
                    updateState { it.copy(users = users, isLoading = false, error = null) }
                }
                .onFailure { throwable ->
                    updateState {
                        it.copy(
                            users = emptyList(),
                            isLoading = false,
                            error = throwable.message ?: "Failed to search users"
                        )
                    }
                }
        }
    }
}
