package com.fitness.app.ui.screens.post

import com.fitness.app.ui.base.BaseViewModel

/**
 * UI State for the Post Screen.
 * Define all the data that your UI needs to display here.
 */
data class PostUiState(
    val title: String = "",
    val description: String = "",
    val workoutType: String = "",
    val duration: String = "",
    val calories: String = "",
    val isPosting: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Post Screen.
 * This class should handle all business logic, such as:
 * - Validating the post content
 * - Communicating with a repository to save the post
 * - Managing the UI state
 */
class PostViewModel : BaseViewModel<PostUiState>(PostUiState()) {

    fun onTitleChanged(newTitle: String) {
        updateState { it.copy(title = newTitle, error = null) }
    }

    fun onDescriptionChanged(newDescription: String) {
        updateState { it.copy(description = newDescription, error = null) }
    }

    fun onWorkoutTypeChanged(newWorkoutType: String) {
        updateState { it.copy(workoutType = newWorkoutType, error = null) }
    }

    fun onDurationChanged(newDuration: String) {
        // Simple numeric validation or filtering could happen here
        updateState { it.copy(duration = newDuration, error = null) }
    }

    fun onCaloriesChanged(newCalories: String) {
        updateState { it.copy(calories = newCalories, error = null) }
    }

    fun clearError() {
        updateState { it.copy(error = null) }
    }

    /**
     * Logic for submitting the post.
     */
    fun submitPost(onSuccess: () -> Unit) {
        if (uiState.value.title.isBlank()) {
            updateState { it.copy(error = "Title cannot be empty") }
            return
        }

        // 1. Set loading state
        updateState { it.copy(isPosting = true) }

        // 2. Perform network/database call here...
        
        // 3. On success:
        onSuccess()
    }
}
