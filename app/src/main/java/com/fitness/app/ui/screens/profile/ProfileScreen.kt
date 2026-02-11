package com.fitness.app.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fitness.app.ui.components.FitTrackHeader
import com.fitness.app.ui.components.PostItem
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSettingsMenu by remember { mutableStateOf(false) }
    val bgColor = Color(0xFFF0F4F8)
    val accentDark = Color(0xFF343E4E)
    val cardBg = Color.White
    val avatarFallbackSeed =
        uiState.profile.username.ifBlank { uiState.profile.name.ifBlank { "user" } }
    val avatarUrl =
        uiState.profile.picture?.takeIf { it.isNotBlank() }
            ?: "https://ui-avatars.com/api/?name=${avatarFallbackSeed}&background=343E4E&color=ffffff"

    val posts by viewModel.posts.collectAsState()
    val isPostsLoading by viewModel.isPostsLoading.collectAsState()
    val postsError by viewModel.postsError.collectAsState()
    val likedPostIds by viewModel.likedPostIds.collectAsState()
    val currentUsername by viewModel.currentUsername.collectAsState()
    val listState = rememberLazyListState()

    val isScrollToEnd by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf false
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= totalItems - 2
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { isScrollToEnd to isPostsLoading }
            .distinctUntilChanged()
            .filter { (scrollToEnd, loading) -> scrollToEnd && !loading }
            .collect { viewModel.loadPosts() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Minimized FitTrack branded header
        FitTrackHeader(
            actions = {
                IconButton(onClick = { showSettingsMenu = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = accentDark
                    )
                }
                DropdownMenu(
                    expanded = showSettingsMenu,
                    onDismissRequest = { showSettingsMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = {
                            showSettingsMenu = false
                            viewModel.logout(context, onLogout)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null
                            )
                        }
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
                // Profile Header Card
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            AsyncImage(
                                model =
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(avatarUrl)
                                        .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(80.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            StatCard(
                                value = uiState.profile.streak.toString(),
                                label = "Streak",
                                accentDark = accentDark
                                )
                            Spacer(modifier = Modifier.width(10.dp))
                            StatCard(
                                value = uiState.profile.posts.toString(),
                                label = "Posts",
                                accentDark = accentDark
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(20.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = uiState.profile.name,
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
                            }
                        }

                        OutlinedButton(
                            onClick = onEditProfile,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = accentDark
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edit Profile",
                                color = accentDark,
                                fontWeight = FontWeight.Bold
                            )
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
                        )
                    )
                }
            } else {
                items(posts, key = { it.id }) { post ->
                    val isLikedByUser =
                        currentUsername != null &&
                            post.likes?.any { it.username == currentUsername } == true
                    PostItem(
                        post = post,
                        isLiked = isLikedByUser || likedPostIds.contains(post.id),
                        onLikeClick = { viewModel.toggleLike(post.id) },
                        onAddComment = { content -> viewModel.addComment(post.id, content) }
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
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    accentDark: Color
) {
    Card(
        modifier = modifier.size(60.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = accentDark
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = accentDark,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
