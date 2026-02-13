package com.fitness.app.ui.screens.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitness.app.data.model.DiscoverUser
import com.fitness.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class DiscoverUiState(
    val query: String = "",
    val users: List<DiscoverUser> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DiscoverViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private var searchJob: Job? = null

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query, error = null)
    }

    fun searchUsers() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(users = emptyList(), isLoading = false, error = null)
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.searchUsers(query)
                .onSuccess { users ->
                    _uiState.value = _uiState.value.copy(users = users, isLoading = false, error = null)
                }
                .onFailure { throwable ->
                    _uiState.value =
                        _uiState.value.copy(
                            users = emptyList(),
                            isLoading = false,
                            error = throwable.message ?: "Failed to search users"
                        )
                }
        }
    }
}
