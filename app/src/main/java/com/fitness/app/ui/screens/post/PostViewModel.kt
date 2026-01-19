package com.fitness.app.ui.screens.post

import com.fitness.app.ui.base.BaseViewModel

/**
 * UI State for the Post Screen.
 * Define all the data that your UI needs to display here.
 */
data class PostUiState(
    val content: String = "",
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

    /**
     * Call this when the user types in the post field.
     */
    fun onContentChanged(newContent: String) {
        updateState { it.copy(content = newContent, error = null) }
    }

    /**
     * Logic for submitting the post.
     */
    fun submitPost(onSuccess: () -> Unit) {
        if (uiState.value.content.isBlank()) {
            updateState { it.copy(error = "Content cannot be empty") }
            return
        }

        // 1. Set loading state
        updateState { it.copy(isPosting = true) }

        // 2. Perform network/database call here...
        
        // 3. On success:
        onSuccess()
    }
}
