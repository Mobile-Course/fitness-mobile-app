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
import com.fitness.app.data.repository.AchievementsRepository
import com.fitness.app.data.repository.PostsRepository
import com.fitness.app.network.NetworkConfig
import java.time.Instant
import com.fitness.app.ui.base.BaseViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class UserProfile(
    val name: String = "User",
    val username: String = "user",
    val email: String = "",
    val picture: String? = null,
    val bio: String = "",
    val workouts: Int = 0,
    val streak: Int = 0,
    val posts: Int = 0,
    val totalXp: Int = 0,
    val level: Int = 1
)

data class ProfileAchievementUi(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val currentTier: String,
    val isUnlocked: Boolean
)

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val achievements: List<ProfileAchievementUi> = emptyList(),
    val isLoading: Boolean = false,
    val isAchievementsLoading: Boolean = false,
    val achievementsError: String? = null,
    val posts: List<Post> = emptyList(),
    val isPostsLoading: Boolean = false,
    val postsError: String? = null,
    val likedPostIds: Set<String> = emptySet(),
    val currentUsername: String? = null
)

class ProfileViewModel : BaseViewModel<ProfileUiState>(ProfileUiState()) {
    private val authRepository = AuthRepository()
    private val achievementsRepository = AchievementsRepository()
    private val postsRepository = PostsRepository()

    private var currentPage = 1
    private var isLastPage = false
    private val limit = 5
    private var currentAuthorId: String? = null

    init {
        viewModelScope.launch {
            combine(
                UserSession.name,
                UserSession.username,
                UserSession.email,
                UserSession.picture,
                combine(
                    UserSession.bio,
                    UserSession.streak,
                    UserSession.totalXp,
                    UserSession.level
                ) { bio, streak, totalXp, level ->
                    ProfileSessionDynamicFields(bio, streak, totalXp, level)
                }
            ) { name, username, email, picture, dynamic ->
                ProfileSessionFields(
                    name = name,
                    username = username,
                    email = email,
                    picture = picture,
                    bio = dynamic.bio,
                    streak = dynamic.streak,
                    totalXp = dynamic.totalXp,
                    level = dynamic.level
                )
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
                                    ?: current.profile.bio,
                            streak = fields.streak ?: current.profile.streak,
                            totalXp = fields.totalXp ?: current.profile.totalXp,
                            level = fields.level ?: current.profile.level
                        )
                    current.copy(profile = updatedProfile, currentUsername = fields.username)
                }
            }
        }

        viewModelScope.launch {
            UserSession.userId.collect { userId ->
                if (userId.isNullOrBlank()) return@collect
                if (userId == currentAuthorId) return@collect
                currentAuthorId = userId
                refreshProfileStats(userId)
                refreshAchievements()
                refreshPosts()
            }
        }

        viewModelScope.launch {
            com.fitness.app.utils.DataInvalidator.refreshProfile.collect { shouldRefresh ->
                if (shouldRefresh) {
                    refreshAchievements()
                    refreshPosts()
                    com.fitness.app.utils.DataInvalidator.refreshProfile.value = false
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

    fun logout(context: Context, onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                authRepository.logout()
            }
            withContext(Dispatchers.IO) {
                AppDatabase.getInstance(context).userDao().clear()
            }
            UserSession.clearPersistedAccessToken(context)
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
        if (uiState.value.isPostsLoading || isLastPage) return

        updateState { it.copy(isPostsLoading = true, postsError = null) }

        viewModelScope.launch {
            try {
                val result = postsRepository.getPostsByAuthor(authorId, currentPage, limit)
                result
                    .onSuccess { response ->
                        updateState { current ->
                            val existingIds = current.posts.map { it.id }.toSet()
                            val newItems = response.items.filter { it.id !in existingIds }
                            val updatedPosts = current.posts + newItems
                            
                            val nextProfile = if (currentPage == 1) {
                                val totalCount = if (response.total > 0) response.total else updatedPosts.size
                                current.profile.copy(posts = totalCount)
                            } else current.profile

                            current.copy(
                                posts = updatedPosts,
                                profile = nextProfile,
                                isPostsLoading = false
                            )
                        }

                        currentPage++
                        isLastPage = response.items.size < limit
                    }
                    .onFailure { e ->
                        updateState { it.copy(postsError = e.message, isPostsLoading = false) }
                    }
            } catch (e: Exception) {
                updateState { it.copy(postsError = e.message, isPostsLoading = false) }
            }
        }
    }

    fun refreshPosts() {
        currentPage = 1
        isLastPage = false
        updateState { it.copy(posts = emptyList()) }
        loadPosts()
    }

    fun refreshProfile() {
        val authorId = currentAuthorId ?: return
        refreshProfileStats(authorId)
        refreshAchievements()
        refreshPosts()
    }

    private fun refreshProfileStats(authorId: String) {
        viewModelScope.launch {
            val profileResult = authRepository.getProfile()
            profileResult
                .onSuccess { profile ->
                    val streak = profile.streak ?: 0
                    UserSession.setUser(
                        streak = streak,
                        totalXp = profile.totalXp ?: 0,
                        level = profile.level ?: 1,
                        aiUsage = profile.aiUsage ?: 0
                    )
                }

            val postsResult = postsRepository.getAllPostsByAuthor(authorId)
            postsResult
                .onSuccess { response ->
                    val count = if (response.total > 0) response.total else response.items.size
                    updateState { current ->
                        current.copy(profile = current.profile.copy(posts = count))
                    }
                }
        }
    }

    private fun refreshAchievements() {
        viewModelScope.launch {
            updateState { it.copy(isAchievementsLoading = true, achievementsError = null) }

            val allResult = achievementsRepository.getAllAchievements()
            val mineResult = achievementsRepository.getMyAchievements()
            val xpResult = achievementsRepository.getMyXp()

            val allAchievements = allResult.getOrNull().orEmpty().filter { it.isActive != false }
            val myAchievementsById =
                mineResult
                    .getOrNull()
                    .orEmpty()
                    .associateBy { it.resolvedAchievementId().orEmpty() }

            val merged =
                allAchievements.mapNotNull { achievement ->
                    val id = achievement.id ?: return@mapNotNull null
                    val myData = myAchievementsById[id]
                    val tier = normalizeTier(myData?.currentTier)
                    ProfileAchievementUi(
                        id = id,
                        title = achievement.name.orEmpty(),
                        description = achievement.description.orEmpty(),
                        icon = achievement.icon.orEmpty(),
                        currentTier = tier,
                        isUnlocked = tier != "none"
                    )
                }

            val mergedOrFallback =
                if (merged.isNotEmpty()) merged
                else {
                    mineResult
                        .getOrNull()
                        .orEmpty()
                        .mapNotNull { myData ->
                            val id = myData.resolvedAchievementId() ?: return@mapNotNull null
                            val tier = normalizeTier(myData.currentTier)
                            ProfileAchievementUi(
                                id = id,
                                title = "Achievement",
                                description = "",
                                icon = "",
                                currentTier = tier,
                                isUnlocked = tier != "none"
                            )
                        }
                }

            val sortedAchievements =
                mergedOrFallback.sortedWith(
                    compareByDescending<ProfileAchievementUi> { it.isUnlocked }.thenBy { it.title }
                )

            updateState { current ->
                val xp = xpResult.getOrNull()
                val nextXp = xp?.totalXp ?: xp?.xp ?: current.profile.totalXp
                val nextLevel = xp?.level ?: current.profile.level
                current.copy(
                    profile = current.profile.copy(totalXp = nextXp, level = nextLevel),
                    achievements = sortedAchievements,
                    isAchievementsLoading = false,
                    achievementsError =
                        allResult.exceptionOrNull()?.message
                            ?: mineResult.exceptionOrNull()?.message
                            ?: xpResult.exceptionOrNull()?.message
                )
            }
        }
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

            val result = postsRepository.likeOrUnlikePost(postId)
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
            val optimisticComments =
                (target.comments ?: emptyList()) + optimisticComment
            val optimisticPost =
                target.copy(comments = optimisticComments)
            
            updateState { state ->
                state.copy(
                    posts = state.posts.map { post -> if (post.id == postId) optimisticPost else post }
                )
            }

            val result = postsRepository.addComment(postId, trimmed)
            result
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

    fun deletePost(postId: String) {
        viewModelScope.launch {
            val result = postsRepository.deletePost(postId)
            result
                .onSuccess {
                    updateState { current ->
                        current.copy(
                            posts = current.posts.filter { it.id != postId },
                            profile =
                                current.profile.copy(
                                    posts = (current.profile.posts - 1).coerceAtLeast(0)
                                )
                        )
                    }
                    com.fitness.app.utils.DataInvalidator.refreshFeed.value = true
                }
                .onFailure { e ->
                    updateState { it.copy(postsError = "Failed to delete post: ${e.message}") }
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
                    // Log error but don't disrupt UI flow
                    android.util.Log.e("ProfileViewModel", "Error fetching post details: ${e.message}", e)
                }
        }
    }
}

private fun normalizeTier(rawTier: String?): String {
    return when (rawTier?.lowercase()) {
        "bronze", "silver", "gold", "diamond" -> rawTier.lowercase()
        else -> "none"
    }
}

private data class ProfileSessionFields(
    val name: String?,
    val username: String?,
    val email: String?,
    val picture: String?,
    val bio: String?,
    val streak: Int?,
    val totalXp: Int?,
    val level: Int?
)

private data class ProfileSessionDynamicFields(
    val bio: String?,
    val streak: Int?,
    val totalXp: Int?,
    val level: Int?
)
