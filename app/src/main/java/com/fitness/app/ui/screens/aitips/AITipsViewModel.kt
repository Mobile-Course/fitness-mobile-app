package com.fitness.app.ui.screens.aitips

import com.fitness.app.ui.base.BaseViewModel

/**
 * UI State for the AI Tips Screen.
 * Use this to keep track of the AI's response, loading states, and user input.
 */
data class AITipsUiState(
    val query: String = "",
    val aiResponse: String? = null,
    val isGenerating: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the AI Tips Screen.
 * Handle your AI integration logic here (e.g., calling Gemini API or a backend).
 */
class AITipsViewModel : BaseViewModel<AITipsUiState>(AITipsUiState()) {

    /**
     * Update the user's question or query for the AI.
     */
    fun onQueryChanged(newQuery: String) {
        updateState { it.copy(query = newQuery, error = null) }
    }

    /**
     * Logical entry point to 'Ask the AI'.
     * Implement your API calls or simulation here.
     */
    fun getAITip() {
        if (uiState.value.query.isBlank()) {
            updateState { it.copy(error = "Please ask something first!") }
            return
        }

        // 1. Show loading/generating state
        updateState { it.copy(isGenerating = true, aiResponse = null) }

        // 2. Integration Tip: Call your AI service here (e.g. Gemini, OpenAI, etc.)
        // GlobalScope.launch { ... } or viewModelScope.launch { ... }
        
        // 3. For now, we stub a success case
        // updateState { it.copy(isGenerating = false, aiResponse = "Here is your personalized fitness tip...") }
    }
}
