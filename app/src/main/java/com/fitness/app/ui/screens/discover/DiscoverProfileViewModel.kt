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
import com.fitness.app.ui.screens.profile.UserProfile
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DiscoverProfileUiState(
    val profile: UserProfile = UserProfile(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DiscoverProfileViewModel : ViewModel() {
    private val postsRepository = PostsRepository()

    private val _uiState = MutableStateFlow(DiscoverProfileUiState())
    val uiState: StateFlow<DiscoverProfileUiState> = _uiState.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _isPostsLoading = MutableStateFlow(false)
    val isPostsLoading: StateFlow<Boolean> = _isPostsLoading.asStateFlow()

    private val _postsError = MutableStateFlow<String?>(null)
    val postsError: StateFlow<String?> = _postsError.asStateFlow()

    private val _likedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val likedPostIds: StateFlow<Set<String>> = _likedPostIds.asStateFlow()

    val currentUsername: StateFlow<String?> = UserSession.username

    private var currentPage = 1
    private var isLastPage = false
    private val limit = 3
    private var currentAuthorId: String? = null

    fun initialize(user: DiscoverUser?) {
        if (user == null) {
            _uiState.value = _uiState.value.copy(error = "User data is unavailable")
            _postsError.value = "User data is unavailable"
            return
        }

        if (currentAuthorId == user.id) return

        currentAuthorId = user.id
        _uiState.value =
            _uiState.value.copy(
                profile =
                    UserProfile(
                        name = user.displayName(),
                        username = user.username,
                        email = "",
                        picture = user.picture,
                        bio = user.description.orEmpty(),
                        streak = 0,
                        posts = 0
                    ),
                isLoading = false,
                error = null
            )

        refreshPosts()
    }

    fun loadPosts() {
        val authorId = currentAuthorId ?: return
        if (_isPostsLoading.value || isLastPage) return

        _isPostsLoading.value = true
        _postsError.value = null

        viewModelScope.launch {
            postsRepository.getPostsByAuthor(authorId, currentPage, limit)
                .onSuccess { response ->
                    val merged = _posts.value.toMutableList().apply { addAll(response.items) }
                    _posts.value = merged

                    if (currentPage == 1) {
                        val totalPosts = if (response.total > 0) response.total else merged.size
                        _uiState.value =
                            _uiState.value.copy(
                                profile = _uiState.value.profile.copy(posts = totalPosts)
                            )
                    }

                    currentPage++
                    isLastPage = response.items.size < limit
                }
                .onFailure { throwable ->
                    _postsError.value = throwable.message ?: "Failed to load posts"
                }
            _isPostsLoading.value = false
        }
    }

    fun refreshPosts() {
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

            postsRepository.likeOrUnlikePost(postId)
                .onSuccess { updatedPost ->
                    _posts.value =
                        _posts.value.map { post ->
                            if (post.id == updatedPost.id) updatedPost else post
                        }

                    if (username != null) {
                        val serverLiked = updatedPost.likes?.any { it.username == username } == true
                        val next = _likedPostIds.value.toMutableSet()
                        if (serverLiked) next.add(postId) else next.remove(postId)
                        _likedPostIds.value = next
                    }
                }
                .onFailure {
                    _posts.value = currentPosts
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
            val currentPosts = _posts.value
            val target = currentPosts.firstOrNull { it.id == postId } ?: return@launch

            val username = currentUsername.value ?: "me"
            val optimisticComment =
                Comment(
                    content = trimmed,
                    author = Author(id = "", username = username, picture = null),
                    createdAt = Instant.now().toString()
                )
            val optimisticPost =
                target.copy(comments = (target.comments ?: emptyList()) + optimisticComment)

            _posts.value =
                currentPosts.map { post -> if (post.id == postId) optimisticPost else post }

            postsRepository.addComment(postId, trimmed)
                .onSuccess { updatedPost ->
                    _posts.value =
                        _posts.value.map { post ->
                            if (post.id == updatedPost.id) updatedPost else post
                        }
                }
                .onFailure {
                    _posts.value = currentPosts
                }
        }
    }
}
