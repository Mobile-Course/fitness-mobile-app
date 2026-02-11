package com.fitness.app.ui.screens.profile

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.fitness.app.auth.UserSession
import com.fitness.app.data.local.AppDatabase
import com.fitness.app.data.model.Author
import com.fitness.app.data.model.Comment
import com.fitness.app.data.model.Like
import com.fitness.app.data.model.Post
import com.fitness.app.data.repository.AuthRepository
import com.fitness.app.data.repository.PostsRepository
import com.fitness.app.network.NetworkConfig
import com.fitness.app.ui.base.BaseViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserProfile(
    val name: String = "User",
    val username: String = "user",
    val email: String = "",
    val picture: String? = null,
    val bio: String = "",
    val workouts: Int = 0,
    val streak: Int = 0,
    val posts: Int = 0
)

data class Achievement(
    val title: String,
    val description: String,
    val icon: String // Using simplified icon representation for demo
)

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val achievements: List<Achievement> = listOf(
        Achievement("Early Bird", "Complete 10 morning workouts", "Trophy")
    ),
    val isLoading: Boolean = false
)

class ProfileViewModel : BaseViewModel<ProfileUiState>(ProfileUiState()) {
    private val authRepository = AuthRepository()
    private val postsRepository = PostsRepository()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isPostsLoading = MutableStateFlow(false)
    val isPostsLoading: StateFlow<Boolean> = _isPostsLoading

    private val _postsError = MutableStateFlow<String?>(null)
    val postsError: StateFlow<String?> = _postsError

    private val _likedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val likedPostIds: StateFlow<Set<String>> = _likedPostIds
    val currentUsername: StateFlow<String?> = UserSession.username

    private var currentPage = 1
    private var isLastPage = false
    private val limit = 3
    private var currentAuthorId: String? = null

    init {
        viewModelScope.launch {
            combine(
                UserSession.name,
                UserSession.username,
                UserSession.email,
                UserSession.picture,
                UserSession.bio
            ) { name, username, email, picture, bio ->
                ProfileSessionFields(name, username, email, picture, bio)
            }.collect { fields ->
                updateState { current ->
                    val updatedProfile =
                        current.profile.copy(
                            name =
                                fields.name?.takeIf { it.isNotBlank() }
                                    ?: current.profile.name,
                            username =
                                fields.username?.takeIf { it.isNotBlank() }
                                    ?: current.profile.username,
                            email =
                                fields.email?.takeIf { it.isNotBlank() }
                                    ?: current.profile.email,
                            picture = fields.picture ?: current.profile.picture,
                            bio =
                                fields.bio?.takeIf { it.isNotBlank() }
                                    ?: current.profile.bio
                        )
                    current.copy(profile = updatedProfile)
                }
            }
        }

        viewModelScope.launch {
            UserSession.userId.collect { userId ->
                if (userId.isNullOrBlank()) return@collect
                if (userId == currentAuthorId) return@collect
                currentAuthorId = userId
                refreshPosts()
            }
        }
    }

    fun logout(context: Context, onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                authRepository.logout()
            }
            withContext(Dispatchers.IO) {
                AppDatabase.getInstance(context).userDao().clear()
            }
            UserSession.clear()
            try {
                NetworkConfig.cookieManager.cookieStore.removeAll()
            } catch (_: Exception) {
            }
            onLoggedOut()
        }
    }

    fun loadPosts() {
        val authorId = currentAuthorId ?: return
        if (_isPostsLoading.value || isLastPage) return

        _isPostsLoading.value = true
        _postsError.value = null

        viewModelScope.launch {
            try {
                val result = postsRepository.getPostsByAuthor(authorId, currentPage, limit)
                result
                    .onSuccess { response ->
                        val currentList = _posts.value.toMutableList()
                        currentList.addAll(response.items)
                        _posts.value = currentList

                        currentPage++
                        isLastPage = response.items.size < limit
                    }
                    .onFailure { e ->
                        _postsError.value = e.message
                    }
            } catch (e: Exception) {
                _postsError.value = e.message
            } finally {
                _isPostsLoading.value = false
            }
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

            val result = postsRepository.likeOrUnlikePost(postId)
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
                    createdAt = ""
                )
            val optimisticComments =
                (target.comments ?: emptyList()) + optimisticComment
            val optimisticPost =
                target.copy(comments = optimisticComments)
            _posts.value =
                currentPosts.map { post ->
                    if (post.id == postId) optimisticPost else post
                }

            val result = postsRepository.addComment(postId, trimmed)
            result
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

private data class ProfileSessionFields(
    val name: String?,
    val username: String?,
    val email: String?,
    val picture: String?,
    val bio: String?
)
