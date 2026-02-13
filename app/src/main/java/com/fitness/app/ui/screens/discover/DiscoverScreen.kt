package com.fitness.app.ui.screens.discover

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.fitness.app.data.model.DiscoverUser
import com.fitness.app.ui.components.FitTrackHeader
import kotlinx.coroutines.delay

@Composable
fun DiscoverScreen(
    onUserClick: (DiscoverUser) -> Unit,
    viewModel: DiscoverViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.query) {
        delay(300)
        viewModel.searchUsers()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        FitTrackHeader()

        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onQueryChanged,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            singleLine = true,
            placeholder = { Text("Search users...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search users"
                )
            },
            colors = OutlinedTextFieldDefaults.colors()
        )

        when {
            uiState.isLoading && uiState.users.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.error ?: "Search failed",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            uiState.query.isBlank() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Search app users",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            uiState.users.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No users found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.users, key = { it.id }) { user ->
                        DiscoverUserRow(
                            user = user,
                            onClick = { onUserClick(user) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoverUserRow(user: DiscoverUser, onClick: () -> Unit) {
    val context = LocalContext.current
    val avatarSeed = user.username.ifBlank { "user" }
    val avatarUrl =
        user.picture?.takeIf { it.isNotBlank() }
            ?: "https://ui-avatars.com/api/?name=$avatarSeed&background=random"

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(avatarUrl).build(),
            contentDescription = "User avatar",
            modifier = Modifier.size(48.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.displayName(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!user.sportType.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = user.sportType,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
