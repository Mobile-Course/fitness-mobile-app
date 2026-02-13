package com.fitness.app.ui.screens.ai_tips

import androidx.lifecycle.viewModelScope
import com.fitness.app.data.repository.CoachRepository
import com.fitness.app.ui.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

enum class ChatRole {
    USER,
    ASSISTANT
}

data class ChatMessage(
    val id: Long,
    val role: ChatRole,
    val content: String
)

data class AITipsUiState(
    val query: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isStreaming: Boolean = false,
    val error: String? = null,
    val initialQuestionSent: Boolean = false
)

class AITipsViewModel : BaseViewModel<AITipsUiState>(AITipsUiState()) {
    private val coachRepository = CoachRepository()
    private var streamJob: Job? = null

    fun onQueryChanged(newQuery: String) {
        updateState { it.copy(query = newQuery, error = null) }
    }

    fun sendInitialQuestionIfNeeded() {
        if (uiState.value.initialQuestionSent) return
        val question = "give me tips based on my profile and last workout"
        updateState { it.copy(initialQuestionSent = true) }
        sendQuestion(question)
    }

    fun sendCurrentQuery() {
        sendQuestion(uiState.value.query)
    }

    private fun sendQuestion(rawQuestion: String) {
        val question = rawQuestion.trim()
        if (question.isBlank() || uiState.value.isStreaming) return

        streamJob?.cancel()

        val userMessage = ChatMessage(
            id = System.currentTimeMillis(),
            role = ChatRole.USER,
            content = question
        )
        val assistantMessage = ChatMessage(
            id = userMessage.id + 1,
            role = ChatRole.ASSISTANT,
            content = ""
        )

        updateState {
            it.copy(
                query = "",
                error = null,
                isStreaming = true,
                messages = it.messages + userMessage + assistantMessage
            )
        }

        streamJob = viewModelScope.launch {
            coachRepository.askStream(question)
                .catch { throwable ->
                    updateState {
                        it.copy(
                            isStreaming = false,
                            error = throwable.message ?: "Failed to fetch AI tips"
                        )
                    }
                }
                .onCompletion {
                    updateState { state -> state.copy(isStreaming = false) }
                }
                .collect { chunk ->
                    updateState { state ->
                        val updated = state.messages.toMutableList()
                        val lastAssistantIndex = updated.indexOfLast { msg -> msg.role == ChatRole.ASSISTANT }
                        if (lastAssistantIndex >= 0) {
                            val current = updated[lastAssistantIndex]
                            val mergedContent = mergeChunk(current.content, chunk)
                            updated[lastAssistantIndex] = current.copy(content = mergedContent)
                        }
                        state.copy(messages = updated)
                    }
                }
        }
    }

    private fun mergeChunk(existing: String, incoming: String): String {
        if (incoming.isBlank()) return existing
        if (incoming.startsWith(existing)) return incoming
        return existing + incoming
    }
}
