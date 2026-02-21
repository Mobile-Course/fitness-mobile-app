package com.fitness.app.ui.screens.discover

import com.fitness.app.auth.UserSession
import com.fitness.app.data.model.DiscoverUser
import com.fitness.app.data.repository.AuthRepository
import com.fitness.app.data.repository.UserProfilesRepository
import com.fitness.app.ui.base.BaseViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

data class DiscoverUiState(
    val query: String = "",
    val users: List<DiscoverUser> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DiscoverViewModel : BaseViewModel<DiscoverUiState>(DiscoverUiState()) {
    private val authRepository = AuthRepository()
    private val userProfilesRepository = UserProfilesRepository()
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
                    val enrichedUsers = enrichUsersWithStats(users)
                    updateState { it.copy(users = enrichedUsers, isLoading = false, error = null) }
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

    private suspend fun enrichUsersWithStats(users: List<DiscoverUser>): List<DiscoverUser> =
        coroutineScope {
            users.map { user ->
                async {
                    val result = userProfilesRepository.getUserProfile(user.id)
                    val profile = result.getOrNull()
                    val xpStats = profile?.xpStats
                    val achievements = profile?.achievements.orEmpty()
                    val unlockedCount =
                        achievements.count { it.currentTier?.lowercase() in setOf("bronze", "silver", "gold", "diamond") }

                    user.copy(
                        totalXp = xpStats?.totalXp ?: user.totalXp,
                        level = xpStats?.level ?: user.level,
                        achievementsCount = if (profile != null) unlockedCount else user.achievementsCount
                    )
                }
            }.awaitAll()
        }
}
