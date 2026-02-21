package com.fitness.app.ui.screens.discover

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitness.app.R
import com.fitness.app.data.model.DiscoverUser
import com.fitness.app.data.model.Post
import com.fitness.app.network.NetworkConfig
import com.fitness.app.ui.components.FitTrackHeader
import com.fitness.app.ui.components.PicassoImage
import com.fitness.app.ui.components.PostItem
import com.fitness.app.ui.screens.profile.ProfileAchievementUi
import com.fitness.app.ui.screens.profile.StatCard

@Composable
fun DiscoverProfileScreen(
    selectedUser: DiscoverUser?,
    onBack: () -> Unit,
    viewModel: DiscoverProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiStateLiveData.observeAsState(DiscoverProfileUiState())
    val posts = uiState.posts
    val isPostsLoading = uiState.isPostsLoading
    val postsError = uiState.postsError
    val likedPostIds = uiState.likedPostIds
    val currentUsername = uiState.currentUsername
    val listState = rememberLazyListState()

    val bgColor = Color(0xFFF0F4F8)
    val accentDark = Color(0xFF343E4E)

    LaunchedEffect(selectedUser?.id) {
        viewModel.initialize(selectedUser)
    }

    val isScrollToEnd by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf false
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= totalItems - 2
        }
    }

    // Trigger load more when scroll is at end and not already loading
    LaunchedEffect(isScrollToEnd, isPostsLoading) {
        if (isScrollToEnd && !isPostsLoading) {
            viewModel.loadPosts()
        }
    }

    val avatarFallbackSeed =
        uiState.profile.username.ifBlank { uiState.profile.name.ifBlank { "user" } }
    val avatarUrl =
        uiState.profile.picture
            ?.takeIf { it.isNotBlank() }
            ?.let { raw ->
                if (raw.startsWith("http")) raw else "${NetworkConfig.BASE_URL}$raw"
            }
            ?: "https://ui-avatars.com/api/?name=${avatarFallbackSeed}&background=343E4E&color=ffffff"

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(bgColor)
    ) {
        FitTrackHeader(
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        androidx.compose.foundation.layout.Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                        ) {
                            PicassoImage(
                                url = avatarUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(80.dp).clip(CircleShape),
                                contentScale = android.widget.ImageView.ScaleType.CENTER_CROP
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Column {
                                androidx.compose.foundation.layout.Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                                    StatCard(
                                        value = uiState.profile.streak.toString(),
                                        label = "Streak",
                                        accentDark = accentDark
                                    )
                                    StatCard(
                                        value = uiState.profile.posts.toString(),
                                        label = "Posts",
                                        accentDark = accentDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                androidx.compose.foundation.layout.Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                                    StatCard(
                                        value = uiState.profile.totalXp.toString(),
                                        label = "XP",
                                        accentDark = accentDark
                                    )
                                    StatCard(
                                        value = uiState.profile.level.toString(),
                                        label = "Level",
                                        accentDark = accentDark
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        androidx.compose.foundation.layout.Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val fullName =
                                    listOf(uiState.profile.name, uiState.profile.lastName)
                                        .map { it.trim() }
                                        .filter { it.isNotBlank() }
                                        .joinToString(" ")
                                        .ifBlank { uiState.profile.name }
                                Text(
                                    text = fullName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = accentDark
                                    )
                                )
                                val username =
                                    uiState.profile.username.ifBlank {
                                        uiState.profile.email.substringBefore("@")
                                    }
                                if (username.isNotBlank()) {
                                    Text(
                                        text = "@${username}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.Gray
                                        )
                                    )
                                }
                                if (uiState.profile.bio.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = uiState.profile.bio,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = accentDark
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                AchievementStrip(
                                    achievements = uiState.achievements,
                                    isLoading = uiState.isAchievementsLoading,
                                    accentDark = accentDark,
                                    edgeColor = bgColor
                                )
                            }
                        }
                    }
                }
            }

            if (posts.isEmpty() && !isPostsLoading && postsError == null) {
                item {
                    Text(
                        text = "No posts yet.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = accentDark,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(posts, key = { it.id }) { post: Post ->
                    val isLikedByUser =
                        (currentUsername != null && post.likes?.any { it.username == currentUsername } == true) ||
                        (post.isLikedByMe)
                    PostItem(
                        post = post,
                        isLiked = isLikedByUser || likedPostIds.contains(post.id),
                        onLikeClick = { viewModel.toggleLike(post.id) },
                        onAddComment = { content -> viewModel.addComment(post.id, content) },
                        onCommentsClick = { viewModel.fetchPostDetails(post.id) }
                    )
                }

                if (isPostsLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                }
            }

            if (postsError != null && posts.isEmpty()) {
                item {
                    Text(
                        text = postsError ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementStrip(
    achievements: List<ProfileAchievementUi>,
    isLoading: Boolean,
    accentDark: Color,
    edgeColor: Color
) {
    if (isLoading && achievements.isEmpty()) {
        Text(
            text = "Loading achievements...",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
        )
        return
    }

    if (achievements.isEmpty()) return

    Column {
        Text(
            text = "Achievements (${achievements.count { it.isUnlocked }})",
            style =
                MaterialTheme.typography.bodySmall.copy(
                    color = accentDark.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold
                )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(achievements, key = { _, item -> item.id }) { index, achievement ->
                    AchievementBadge(achievement = achievement, accentDark = accentDark, fallbackIndex = index)
                }
            }

            val fadeWidth = 28.dp
            Box(
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .width(fadeWidth)
                        .background(
                            brush =
                                Brush.horizontalGradient(
                                    colors = listOf(edgeColor, edgeColor.copy(alpha = 0f))
                                )
                        )
            )
            Box(
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(fadeWidth)
                        .background(
                            brush =
                                Brush.horizontalGradient(
                                    colors = listOf(edgeColor.copy(alpha = 0f), edgeColor)
                                )
                        )
            )
        }
    }
}

@Composable
private fun AchievementBadge(
    achievement: ProfileAchievementUi,
    accentDark: Color,
    fallbackIndex: Int
) {
    val context = LocalContext.current
    var showTooltip by remember { mutableStateOf(false) }
    val resId = remember(achievement.icon, achievement.title, fallbackIndex) {
        resolveAchievementIconResId(
            context = context,
            rawIcon = achievement.icon,
            title = achievement.title,
            fallbackIndex = fallbackIndex
        )
    }
    val lockFilter =
        if (achievement.isUnlocked) null
        else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

    Box {
        Card(
            modifier =
                Modifier
                    .size(64.dp)
                    .clickable { showTooltip = true },
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, tierColor(achievement.currentTier, accentDark))
        ) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = achievement.title,
                modifier = Modifier.fillMaxSize().padding(5.dp),
                colorFilter = lockFilter
            )
        }

        DropdownMenu(
            expanded = showTooltip,
            onDismissRequest = { showTooltip = false }
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = achievement.title.ifBlank { "Achievement" },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = accentDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = achievement.description.ifBlank { "Complete tasks to unlock this achievement." },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tier: ${achievement.currentTier.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = tierColor(achievement.currentTier, accentDark)
                )
            }
        }
    }
}

private fun resolveAchievementIconResId(
    context: android.content.Context,
    rawIcon: String,
    title: String,
    fallbackIndex: Int
): Int {
    val allIcons =
        listOf(
            R.drawable.early_bird,
            R.drawable.first_steps,
            R.drawable.consistency_master,
            R.drawable.workout_streak,
            R.drawable.volume_king,
            R.drawable.pain_free,
            R.drawable.ai_focused
        )

    val normalizedIcon =
        rawIcon.substringBeforeLast(".")
            .lowercase()
            .replace("-", "_")
            .replace(" ", "_")
            .replace("_icon", "")

    val normalizedTitle =
        title.lowercase()
            .replace("-", "_")
            .replace(" ", "_")

    val titleMap =
        mapOf(
            "early_bird" to R.drawable.early_bird,
            "first_steps" to R.drawable.first_steps,
            "consistency_master" to R.drawable.consistency_master,
            "workout_streak" to R.drawable.workout_streak,
            "volume_king" to R.drawable.volume_king,
            "pain_free" to R.drawable.pain_free,
            "ai_focused" to R.drawable.ai_focused
        )

    titleMap.entries.firstOrNull { normalizedTitle.contains(it.key) }?.let { return it.value }

    if (normalizedIcon.isNotBlank()) {
        val fromName = context.resources.getIdentifier(normalizedIcon, "drawable", context.packageName)
        if (fromName != 0) return fromName
    }

    return allIcons[fallbackIndex % allIcons.size]
}

private fun tierColor(tier: String, accentDark: Color): Color {
    return when (tier.lowercase()) {
        "bronze" -> Color(0xFFB08D57)
        "silver" -> Color(0xFFBFC4CE)
        "gold" -> Color(0xFFFFD54F)
        "diamond" -> Color(0xFF64B5F6)
        else -> accentDark.copy(alpha = 0.25f)
    }
}
