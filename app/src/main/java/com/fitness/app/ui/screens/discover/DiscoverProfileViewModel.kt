package com.fitness.app.ui.screens.discover

import androidx.lifecycle.viewModelScope
import com.fitness.app.auth.UserSession
import com.fitness.app.ui.base.BaseViewModel
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
    val error: String? = null,
    val posts: List<Post> = emptyList(),
    val isPostsLoading: Boolean = false,
    val postsError: String? = null,
    val likedPostIds: Set<String> = emptySet(),
    val currentUsername: String? = null
)

class DiscoverProfileViewModel : BaseViewModel<DiscoverProfileUiState>(DiscoverProfileUiState()) {
    private val postsRepository = PostsRepository()

    private var currentPage = 1
    private var isLastPage = false
    private val limit = 5
    private var currentAuthorId: String? = null

    init {
        viewModelScope.launch {
            UserSession.username.collect { username ->
                updateState { it.copy(currentUsername = username) }
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

    fun initialize(user: DiscoverUser?) {
        if (user == null) {
            updateState { it.copy(error = "User data is unavailable", postsError = "User data is unavailable") }
            return
        }

        if (currentAuthorId == user.id) return

        currentAuthorId = user.id
        updateState {
            it.copy(
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
        }

        refreshPosts()
    }

    fun loadPosts() {
        val authorId = currentAuthorId ?: return
        if (uiState.value.isPostsLoading || isLastPage) return

        updateState { it.copy(isPostsLoading = true, postsError = null) }

        viewModelScope.launch {
            postsRepository.getPostsByAuthor(authorId, currentPage, limit)
                .onSuccess { response ->
                    updateState { current ->
                        val existingIds = current.posts.map { it.id }.toSet()
                        val newItems = response.items.filter { it.id !in existingIds }
                        val merged = current.posts + newItems
                        
                        val nextProfile = if (currentPage == 1) {
                            val totalPosts = if (response.total > 0) response.total else merged.size
                            current.profile.copy(posts = totalPosts)
                        } else current.profile

                        current.copy(
                            posts = merged,
                            profile = nextProfile,
                            isPostsLoading = false
                        )
                    }

                    currentPage++
                    isLastPage = response.items.size < limit
                }
                .onFailure { throwable ->
                    updateState { it.copy(postsError = throwable.message ?: "Failed to load posts", isPostsLoading = false) }
                }
        }
    }

    fun refreshPosts() {
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
                    likeNumber = optimisticLikeNumber
                )

            updateState { state ->
                val optimisticSet = state.likedPostIds.toMutableSet()
                if (wasLiked) optimisticSet.remove(postId) else optimisticSet.add(postId)
                state.copy(
                    posts = state.posts.map { post -> if (post.id == postId) optimisticPost else post },
                    likedPostIds = optimisticSet
                )
            }

            postsRepository.likeOrUnlikePost(postId)
                .onSuccess { updatedPost ->
                    updateState { state ->
                        val next = state.likedPostIds.toMutableSet()
                        if (username != null) {
                            val serverLiked = updatedPost.likes?.any { it.username == username } == true
                            if (serverLiked) next.add(postId) else next.remove(postId)
                        }
                        state.copy(
                            posts = state.posts.map { post -> if (post.id == updatedPost.id) updatedPost else post },
                            likedPostIds = next
                        )
                    }
                }
                .onFailure {
                    updateState { state ->
                        val revertedSet = state.likedPostIds.toMutableSet()
                        if (wasLiked) revertedSet.add(postId) else revertedSet.remove(postId)
                        state.copy(
                            posts = currentPosts,
                            likedPostIds = revertedSet
                        )
                    }
                }
        }
    }

    fun addComment(postId: String, content: String) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            val currentState = uiState.value
            val currentPosts = currentState.posts
            val target = currentPosts.firstOrNull { it.id == postId } ?: return@launch

            val username = currentState.currentUsername ?: "me"
            val optimisticComment =
                Comment(
                    content = trimmed,
                    author = Author(id = "", username = username, picture = null),
                    createdAt = Instant.now().toString()
                )
            val optimisticPost =
                target.copy(comments = (target.comments ?: emptyList()) + optimisticComment)

            updateState { state ->
                state.copy(
                    posts = state.posts.map { post -> if (post.id == postId) optimisticPost else post }
                )
            }

            postsRepository.addComment(postId, trimmed)
                .onSuccess { updatedPost ->
                    updateState { state ->
                        state.copy(
                            posts = state.posts.map { post -> if (post.id == updatedPost.id) updatedPost else post }
                        )
                    }
                }
                .onFailure {
                    updateState { it.copy(posts = currentPosts) }
                }

        }
    }

    fun fetchPostDetails(postId: String) {
        viewModelScope.launch {
            postsRepository.getPost(postId)
                .onSuccess { updatedPost ->
                    updateState { state ->
                        state.copy(
                            posts = state.posts.map { post -> if (post.id == updatedPost.id) updatedPost else post }
                        )
                    }
                }
                .onFailure { e ->
                    android.util.Log.e("DiscoverProfileViewModel", "Error fetching post details: ${e.message}", e)
                }
        }
    }
}
