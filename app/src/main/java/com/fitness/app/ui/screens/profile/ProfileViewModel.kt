package com.fitness.app.ui.screens.profile

import com.fitness.app.ui.base.BaseViewModel

data class UserProfile(
    val name: String = "guy",
    val username: String = "user",
    val bio: String = "Fitness enthusiast | Building strength 💪",
    val workouts: Int = 0,
    val streak: Int = 0,
    val posts: Int = 0
)

data class Achievement(
    val title: String,
    val description: String,
    val icon: String // Using simplified icon representation for demo
)

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val achievements: List<Achievement> = listOf(
        Achievement("Early Bird", "Complete 10 morning workouts", "🏆")
    ),
    val isLoading: Boolean = false
)

class ProfileViewModel : BaseViewModel<ProfileUiState>(ProfileUiState()) {
    // Methods for updating profile or refreshing stats could go here
}
