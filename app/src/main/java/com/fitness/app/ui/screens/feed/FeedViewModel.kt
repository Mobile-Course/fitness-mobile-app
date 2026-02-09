package com.fitness.app.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitness.app.data.model.Post
import com.fitness.app.data.repository.PostsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {
    private val repository = PostsRepository()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentPage = 1
    private var isLastPage = false
    private val limit = 3

    init {
        loadPosts()
    }

    fun loadPosts() {
        if (_isLoading.value || isLastPage) return

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                android.util.Log.d("FeedViewModel", "Fetching page $currentPage with limit $limit")
                val result = repository.getPosts(currentPage, limit)
                result
                        .onSuccess { response ->
                            android.util.Log.d(
                                    "FeedViewModel",
                                    "Success: Received ${response.items.size} items"
                            )
                            val currentList = _posts.value.toMutableList()
                            currentList.addAll(response.items)
                            _posts.value = currentList

                            currentPage++
                            isLastPage = response.items.size < limit
                            android.util.Log.d(
                                    "FeedViewModel",
                                    "Next page: $currentPage, isLastPage: $isLastPage"
                            )
                        }
                        .onFailure { e ->
                            android.util.Log.e(
                                    "FeedViewModel",
                                    "Error fetching posts: ${e.message}",
                                    e
                            )
                            _error.value = e.message
                        }
            } catch (e: Exception) {
                android.util.Log.e("FeedViewModel", "Unexpected error: ${e.message}", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        currentPage = 1
        isLastPage = false
        _posts.value = emptyList()
        loadPosts()
    }
}
