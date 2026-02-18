package com.fitness.app.ui.screens.feed

import androidx.lifecycle.viewModelScope
import com.fitness.app.auth.UserSession
import com.fitness.app.data.model.Like
import com.fitness.app.data.model.Post
import com.fitness.app.data.model.Author
import com.fitness.app.data.model.Comment
import com.fitness.app.data.repository.PostsRepository
import com.fitness.app.network.NetworkConfig
import com.fitness.app.ui.base.BaseViewModel
import java.time.Instant
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val likedPostIds: Set<String> = emptySet(),
    val currentUsername: String? = null
)

class FeedViewModel : BaseViewModel<FeedUiState>(FeedUiState()) {
    private val repository = PostsRepository()

    private var currentPage = 1
    private var isLastPage = false
    private val limit = 5

    init {
        // Sync currentUsername from UserSession
        viewModelScope.launch {
            UserSession.username.collect { username ->
                updateState { it.copy(currentUsername = username) }
            }
        }

        loadPosts()
        viewModelScope.launch {
            com.fitness.app.utils.DataInvalidator.refreshFeed.collect { shouldRefresh ->
                if (shouldRefresh) {
                    refresh()
                    com.fitness.app.utils.DataInvalidator.refreshFeed.value = false
                }
            }
        }

        // Listen for individual post updates (cross-screen sync)
        viewModelScope.launch {
            com.fitness.app.utils.DataInvalidator.postUpdates.collect { updatedPost ->
                updateState { state ->
                    val hasPost = state.posts.any { it.id == updatedPost.id }
                    if (!hasPost) return@updateState state
                    
                    val updatedLikedPostIds = state.likedPostIds.toMutableSet()
                    if (updatedPost.isLikedByMe) {
                        updatedLikedPostIds.add(updatedPost.id)
                    } else {
                        updatedLikedPostIds.remove(updatedPost.id)
                    }

                    state.copy(
                        posts = state.posts.map { post ->
                            if (post.id == updatedPost.id) updatedPost else post
                        },
                        likedPostIds = updatedLikedPostIds
                    )
                }
            }
        }

        // Listen for post deletions (cross-screen sync)
        viewModelScope.launch {
            com.fitness.app.utils.DataInvalidator.postDeletions.collect { deletedPostId ->
                updateState { state ->
                    val hasPost = state.posts.any { it.id == deletedPostId }
                    if (!hasPost) return@updateState state
                    
                    state.copy(
                        posts = state.posts.filter { it.id != deletedPostId }
                    )
                }
            }
        }
    }

    fun loadPosts() {
        if (uiState.value.isLoading || isLastPage) return

        updateState { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                android.util.Log.d("FeedViewModel", "Fetching page $currentPage with limit $limit")
                val result = repository.getPosts(currentPage, limit)
                result
                        .onSuccess { response ->
                            val items = response.items
                            
                            android.util.Log.d(
                                    "FeedViewModel",
                                    "Success: Received ${items.size} items"
                            )
                            updateState { state ->
                                val existingIds = state.posts.map { it.id }.toSet()
                                val newItems = items.filter { it.id !in existingIds }
                                state.copy(
                                    posts = state.posts + newItems,
                                    isLoading = false
                                )
                            }

                            currentPage++
                            isLastPage = items.size < limit
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
                            updateState { it.copy(error = e.message, isLoading = false) }
                        }
            } catch (e: Exception) {
                android.util.Log.e("FeedViewModel", "Unexpected error: ${e.message}", e)
                updateState { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun refresh() {
        currentPage = 1
        isLastPage = false
        updateState { it.copy(posts = emptyList()) }
        loadPosts()
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val currentState = uiState.value
            val currentPosts = currentState.posts
            val target = currentPosts.firstOrNull { it.id == postId } ?: return@launch

            val username = currentState.currentUsername
            val wasLiked =
                    (username != null && target.likes?.any { it.username == username } == true) ||
                            target.isLikedByMe ||
                            currentState.likedPostIds.contains(postId)

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
                            likeNumber = optimisticLikeNumber,
                            isLikedByMe = !wasLiked
                    )

            updateState { state ->
                val optimisticSet = state.likedPostIds.toMutableSet()
                if (wasLiked) optimisticSet.remove(postId) else optimisticSet.add(postId)
                state.copy(
                    posts = state.posts.map { post -> if (post.id == postId) optimisticPost else post },
                    likedPostIds = optimisticSet
                )
            }

            android.util.Log.d(
                    "FeedViewModel",
                    "Like request cookies: ${NetworkConfig.dumpCookies()}"
            )
            val result = repository.likeOrUnlikePost(postId)
            result
                    .onSuccess { updatedPost ->
                        updateState { state ->
                            val next = state.likedPostIds.toMutableSet()
                            if (username != null) {
                                val serverLiked =
                                        updatedPost.likes?.any { it.username == username } == true
                                if (serverLiked) next.add(postId) else next.remove(postId)
                            }
                            state.copy(
                                posts = state.posts.map { post ->
                                    if (post.id == updatedPost.id) updatedPost else post
                                },
                                likedPostIds = next
                            )
                        }
                    }
                    .onFailure { e ->
                        updateState { state ->
                            val revertedSet = state.likedPostIds.toMutableSet()
                            if (wasLiked) revertedSet.add(postId) else revertedSet.remove(postId)
                            state.copy(
                                posts = currentPosts,
                                likedPostIds = revertedSet
                            )
                        }
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
            val currentPosts = uiState.value.posts
            val target = currentPosts.firstOrNull { it.id == postId } ?: return@launch

            val username = uiState.value.currentUsername ?: "me"
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
            
            updateState { state ->
                state.copy(
                    posts = state.posts.map { post -> if (post.id == postId) optimisticPost else post }
                )
            }

            val result = repository.addComment(postId, trimmed)
            result
                    .onSuccess { updatedPost ->
                        updateState { state ->
                            state.copy(
                                posts = state.posts.map { post -> if (post.id == updatedPost.id) updatedPost else post }
                            )
                        }
                    }
                    .onFailure { e ->
                        updateState { it.copy(posts = currentPosts) }
                        android.util.Log.e(
                                "FeedViewModel",
                                "Error adding comment: ${e.message}",
                                e
                        )
                    }
        }
    }

    fun fetchPostDetails(postId: String) {
        viewModelScope.launch {
            repository.getPost(postId)
                .onSuccess { updatedPost ->
                    updateState { state ->
                        state.copy(
                            posts = state.posts.map { post -> if (post.id == updatedPost.id) updatedPost else post }
                        )
                    }
                }
                .onFailure { e ->
                    android.util.Log.e("FeedViewModel", "Error fetching post details: ${e.message}", e)
                }
        }
    }
}
