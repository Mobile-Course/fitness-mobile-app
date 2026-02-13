package com.fitness.app.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitness.app.auth.UserSession
import com.fitness.app.data.model.Like
import com.fitness.app.data.model.Post
import com.fitness.app.data.model.Author
import com.fitness.app.data.model.Comment
import com.fitness.app.data.repository.PostsRepository
import com.fitness.app.network.NetworkConfig
import java.time.Instant
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

    private val _likedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val likedPostIds: StateFlow<Set<String>> = _likedPostIds
    val currentUsername: StateFlow<String?> = UserSession.username

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

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val currentPosts = _posts.value
            val target = currentPosts.firstOrNull { it.id == postId } ?: return@launch

            val username = currentUsername.value
            val wasLiked =
                    (username != null && target.likes?.any { it.username == username } == true) ||
                            _likedPostIds.value.contains(postId)

            val optimisticLikes =
                    if (username == null) target.likes
                    else {
                        val list = (target.likes ?: emptyList()).toMutableList()
                        if (wasLiked) {
                            list.removeAll { it.username == username }
                        } else {
                            list.add(Like(username = username, picture = null))
                        }
                        list
                    }

            val optimisticLikeNumber =
                    if (wasLiked) (target.likeNumber - 1).coerceAtLeast(0)
                    else target.likeNumber + 1

            val optimisticPost =
                    target.copy(
                            likes = optimisticLikes,
                            likeNumber = optimisticLikeNumber
                    )

            _posts.value =
                    currentPosts.map { post -> if (post.id == postId) optimisticPost else post }

            val optimisticSet = _likedPostIds.value.toMutableSet()
            if (wasLiked) optimisticSet.remove(postId) else optimisticSet.add(postId)
            _likedPostIds.value = optimisticSet

            android.util.Log.d(
                    "FeedViewModel",
                    "Like request cookies: ${NetworkConfig.dumpCookies()}"
            )
            val result = repository.likeOrUnlikePost(postId)
            result
                    .onSuccess { updatedPost ->
                        _posts.value =
                                _posts.value.map { post ->
                                    if (post.id == updatedPost.id) updatedPost else post
                                }

                        if (username != null) {
                            val serverLiked =
                                    updatedPost.likes?.any { it.username == username } == true
                            val next = _likedPostIds.value.toMutableSet()
                            if (serverLiked) next.add(postId) else next.remove(postId)
                            _likedPostIds.value = next
                        }
                    }
                    .onFailure { e ->
                        _posts.value = currentPosts
                        _likedPostIds.value =
                                if (wasLiked) _likedPostIds.value + postId
                                else _likedPostIds.value - postId
                        android.util.Log.e(
                                "FeedViewModel",
                                "Error liking post: ${e.message}",
                                e
                        )
                    }
        }
    }

    fun addComment(postId: String, content: String) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val currentPosts = _posts.value
            val target = currentPosts.firstOrNull { it.id == postId } ?: return@launch

            val username = currentUsername.value ?: "me"
            val optimisticComment =
                    Comment(
                            content = trimmed,
                            author = Author(id = "", username = username, picture = null),
                            createdAt = Instant.now().toString()
                    )
            val optimisticComments =
                    (target.comments ?: emptyList()) + optimisticComment
            val optimisticPost =
                    target.copy(comments = optimisticComments)
            _posts.value =
                    currentPosts.map { post ->
                        if (post.id == postId) optimisticPost else post
                    }

            val result = repository.addComment(postId, trimmed)
            result
                    .onSuccess { updatedPost ->
                        _posts.value =
                                _posts.value.map { post ->
                                    if (post.id == updatedPost.id) updatedPost else post
                                }
                    }
                    .onFailure { e ->
                        _posts.value = currentPosts
                        android.util.Log.e(
                                "FeedViewModel",
                                "Error adding comment: ${e.message}",
                                e
                        )
                    }
        }
    }
}
