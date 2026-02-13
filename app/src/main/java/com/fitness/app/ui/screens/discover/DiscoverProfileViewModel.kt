package com.fitness.app.ui.screens.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitness.app.auth.UserSession
import com.fitness.app.data.model.Author
import com.fitness.app.data.model.Comment
import com.fitness.app.data.model.DiscoverUser
import com.fitness.app.data.model.Like
import com.fitness.app.data.model.Post
import com.fitness.app.data.repository.PostsRepository
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DiscoverProfileUiState(
    val user: DiscoverUser? = null,
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DiscoverProfileViewModel : ViewModel() {
    private val postsRepository = PostsRepository()
    private val currentUsername: StateFlow<String?> = UserSession.username

    private val _uiState = MutableStateFlow(DiscoverProfileUiState())
    val uiState: StateFlow<DiscoverProfileUiState> = _uiState.asStateFlow()

    private val _likedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val likedPostIds: StateFlow<Set<String>> = _likedPostIds.asStateFlow()

    private var loadedUserId: String? = null

    fun initialize(user: DiscoverUser?) {
        if (user == null) {
            _uiState.value = DiscoverProfileUiState(error = "User data is unavailable")
            return
        }
        if (loadedUserId == user.id && _uiState.value.posts.isNotEmpty()) return

        loadedUserId = user.id
        _uiState.value = _uiState.value.copy(user = user, isLoading = true, error = null)

        viewModelScope.launch {
            postsRepository.getPostsByAuthor(user.id, page = 1, limit = 50)
                .onSuccess { response ->
                    _uiState.value =
                        _uiState.value.copy(
                            user = user,
                            posts = response.items,
                            isLoading = false,
                            error = null
                        )
                }
                .onFailure { throwable ->
                    _uiState.value =
                        _uiState.value.copy(
                            user = user,
                            posts = emptyList(),
                            isLoading = false,
                            error = throwable.message ?: "Failed to load user profile"
                        )
                }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val currentPosts = _uiState.value.posts
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

            _uiState.value =
                _uiState.value.copy(
                    posts = currentPosts.map { post -> if (post.id == postId) optimisticPost else post }
                )

            val optimisticSet = _likedPostIds.value.toMutableSet()
            if (wasLiked) optimisticSet.remove(postId) else optimisticSet.add(postId)
            _likedPostIds.value = optimisticSet

            postsRepository.likeOrUnlikePost(postId)
                .onSuccess { updatedPost ->
                    _uiState.value =
                        _uiState.value.copy(
                            posts =
                                _uiState.value.posts.map { post ->
                                    if (post.id == updatedPost.id) updatedPost else post
                                }
                        )
                    if (username != null) {
                        val serverLiked = updatedPost.likes?.any { it.username == username } == true
                        val next = _likedPostIds.value.toMutableSet()
                        if (serverLiked) next.add(postId) else next.remove(postId)
                        _likedPostIds.value = next
                    }
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(posts = currentPosts)
                    _likedPostIds.value =
                        if (wasLiked) _likedPostIds.value + postId
                        else _likedPostIds.value - postId
                }
        }
    }

    fun addComment(postId: String, content: String) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val currentPosts = _uiState.value.posts
            val target = currentPosts.firstOrNull { it.id == postId } ?: return@launch

            val username = currentUsername.value ?: "me"
            val optimisticComment =
                Comment(
                    content = trimmed,
                    author = Author(id = "", username = username, picture = null),
                    createdAt = Instant.now().toString()
                )
            val optimisticPost = target.copy(comments = (target.comments ?: emptyList()) + optimisticComment)

            _uiState.value =
                _uiState.value.copy(
                    posts = currentPosts.map { post -> if (post.id == postId) optimisticPost else post }
                )

            postsRepository.addComment(postId, trimmed)
                .onSuccess { updatedPost ->
                    _uiState.value =
                        _uiState.value.copy(
                            posts =
                                _uiState.value.posts.map { post ->
                                    if (post.id == updatedPost.id) updatedPost else post
                                }
                        )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(posts = currentPosts)
                }
        }
    }
}
