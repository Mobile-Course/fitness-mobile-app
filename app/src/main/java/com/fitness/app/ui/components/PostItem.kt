package com.fitness.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fitness.app.data.model.Post
import android.util.Base64

@Composable
fun PostItem(
        post: Post,
        isLiked: Boolean,
        onLikeClick: () -> Unit,
        onAddComment: (String) -> Unit
) {
    if (!post.pictures.isNullOrEmpty()) {
        android.util.Log.d("PostItem", "First Image: ${post.pictures.first().take(50)}...")
    }

    Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            // Header: Avatar + Username
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
            ) {
                AsyncImage(
                        model =
                                ImageRequest.Builder(LocalContext.current)
                                        .data(
                                                post.author.picture
                                                        ?: "https://ui-avatars.com/api/?name=${post.author.username}&background=random"
                                        )
                                        .listener(
                                                onError = { _, result ->
                                                    android.util.Log.e(
                                                            "PostItem",
                                                            "Avatar load failed: ${result.throwable.message}",
                                                            result.throwable
                                                    )
                                                }
                                        )
                                        .build(),
                        contentDescription = "Author Avatar",
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                            text = post.author.username,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = "2 hours ago", // Placeholder for timestamp
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                    )
                }
            }

            // Post Image (if any)
            if (!post.pictures.isNullOrEmpty()) {
                val imageUrl = post.pictures.first()
                if (imageUrl.isNotBlank()) {
                    val imageModel = remember(imageUrl) {
                        resolveImageModel(imageUrl)
                    }
                    AsyncImage(
                            model =
                                    ImageRequest.Builder(LocalContext.current)
                                            .data(imageModel)
                                            .listener(
                                                    onStart = {
                                                        android.util.Log.d(
                                                                "PostItem",
                                                                "Image request started: ${imageUrl.take(50)}..."
                                                        )
                                                    },
                                                    onSuccess = { _, _ ->
                                                        android.util.Log.d(
                                                                "PostItem",
                                                                "Image loaded successfully"
                                                        )
                                                    },
                                                    onError = { _, result ->
                                                        android.util.Log.e(
                                                                "PostItem",
                                                                "Image load failed: ${result.throwable.message}",
                                                                result.throwable
                                                        )
                                                    }
                                            )
                                            .build(),
                            contentDescription = "Post Image",
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Content: Title + Description
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (post.description != null) {
                    Text(text = post.description, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            var showCommentInput by rememberSaveable(post.id) { mutableStateOf(false) }

            Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                        onClick = onLikeClick,
                        modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                            imageVector =
                                    if (isLiked) Icons.Filled.Favorite
                                    else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint =
                                    if (isLiked) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                        text = "${post.likeNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                )

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                        onClick = { showCommentInput = !showCommentInput },
                        modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                        text = "${(post.comments ?: emptyList()).size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val comments = post.comments ?: emptyList()
            if (comments.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                            text = "Comments",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    comments.forEach { comment ->
                        Text(
                                text = "${comment.author.username}: ${comment.content}",
                                style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (showCommentInput) {
                var commentText by rememberSaveable(post.id) { mutableStateOf("") }
                Row(
                        modifier =
                                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Add a comment") },
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            textStyle = TextStyle(fontSize = 12.sp),
                            colors =
                                    OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                    )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                            onClick = {
                                val trimmed = commentText.trim()
                                if (trimmed.isNotEmpty()) {
                                    onAddComment(trimmed)
                                    commentText = ""
                                    showCommentInput = false
                                }
                            },
                            modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Outlined.Send,
                                contentDescription = "Send comment",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun resolveImageModel(raw: String): Any? {
    if (raw.startsWith("http://") || raw.startsWith("https://")) {
        return raw
    }

    val base64 = raw.substringAfter("base64,", raw)
    return try {
        Base64.decode(base64, Base64.DEFAULT)
    } catch (e: IllegalArgumentException) {
        android.util.Log.e("PostItem", "Invalid base64 image data", e)
        null
    }
}
