package com.fitness.app.ui.screens.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fitness.app.auth.UserSession
import com.fitness.app.data.model.DiscoverUser
import com.fitness.app.ui.components.FitTrackHeader
import com.fitness.app.ui.components.PostItem

@Composable
fun DiscoverProfileScreen(
    selectedUser: DiscoverUser?,
    onBack: () -> Unit,
    viewModel: DiscoverProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val likedPostIds by viewModel.likedPostIds.collectAsState()
    val currentUsername by UserSession.username.collectAsState()

    LaunchedEffect(selectedUser?.id) {
        viewModel.initialize(selectedUser)
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                UserSummaryCard(user = uiState.user)
            }

            if (uiState.isLoading && uiState.posts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.error != null) {
                item {
                    Text(
                        text = uiState.error ?: "Failed to load user profile",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else if (uiState.posts.isEmpty()) {
                item {
                    Text(
                        text = "No posts yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(uiState.posts, key = { it.id }) { post ->
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
            }
        }
    }
}

@Composable
private fun UserSummaryCard(user: DiscoverUser?) {
    val context = LocalContext.current
    val username = user?.username.orEmpty()
    val avatarUrl =
        user?.picture?.takeIf { it.isNotBlank() }
            ?: "https://ui-avatars.com/api/?name=${username.ifBlank { "user" }}&background=random"

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(avatarUrl).build(),
                contentDescription = "Profile picture",
                modifier = Modifier.size(72.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                Text(
                    text = user?.displayName() ?: "Unknown user",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (username.isNotBlank()) {
                    Text(
                        text = "@$username",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!user?.sportType.isNullOrBlank()) {
                    Text(
                        text = user?.sportType.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (!user?.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user?.description.orEmpty(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
