package com.fitness.app.ui.components

import android.content.Intent
import android.util.Base64
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fitness.app.data.model.Post
import com.fitness.app.network.NetworkConfig
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun PostItem(
        post: Post,
        isLiked: Boolean,
        isAuthor: Boolean = false,
        onLikeClick: () -> Unit,
        onAddComment: (String) -> Unit,
        onDeleteClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val authorDisplayName = post.author.name?.takeIf { it.isNotBlank() } ?: post.author.username
    val imageSource = post.src?.takeIf { it.isNotBlank() } ?: post.pictures?.firstOrNull { it.isNotBlank() }
    val workout = post.workoutDetails
    val hasWorkoutDetails =
            workout?.type?.isNotBlank() == true ||
                    workout?.duration != null ||
                    workout?.calories != null

    Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
            ) {
                AsyncImage(
                        model =
                                ImageRequest.Builder(context)
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
                            text = authorDisplayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = formatRelativeTime(post.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                    )
                }
                if (isAuthor) {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Post",
                            tint = Color.Gray
                        )
                    }
                }
            }

            if (!imageSource.isNullOrBlank()) {
                val imageModel = remember(imageSource) { resolveImageModel(imageSource) }
                AsyncImage(
                        model =
                                ImageRequest.Builder(context)
                                        .data(imageModel)
                                        .listener(
                                                onStart = {
                                                    android.util.Log.d(
                                                            "PostItem",
                                                            "Image request started: ${imageSource.take(50)}..."
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

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (!post.description.isNullOrBlank()) {
                    Text(text = post.description, style = MaterialTheme.typography.bodyMedium)
                }

                if (hasWorkoutDetails) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                        text = workout?.type?.takeIf { it.isNotBlank() } ?: "Workout",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                )
                                workout?.duration?.let { minutes ->
                                    Text(
                                            text = "$minutes min",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                    )
                                }
                            }
                            workout?.calories?.let { calories ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                        text = "$calories calories burned",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            var showCommentInput by rememberSaveable(post.id) { mutableStateOf(false) }
            var showAllComments by rememberSaveable(post.id) { mutableStateOf(false) }

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

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                        onClick = {
                            val shareLink = "${NetworkConfig.BASE_URL}/posts/${post.id}"
                            val shareText =
                                    "Check out this workout post on FitTrack:\n$shareLink"
                            val intent =
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, post.title)
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                            context.startActivity(Intent.createChooser(intent, "Share post"))
                        },
                        modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val comments = post.comments ?: emptyList()
            if (comments.isNotEmpty()) {
                val visibleComments =
                        if (showAllComments || comments.size <= 2) comments
                        else comments.takeLast(2)

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                            text = "Comments",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    visibleComments.forEach { comment ->
                        Text(
                                text = "${comment.author.username}: ${comment.content}",
                                style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (comments.size > 2) {
                        TextButton(
                                onClick = { showAllComments = !showAllComments },
                                contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                    text =
                                            if (showAllComments) "Show less"
                                            else "Show more comments"
                            )
                        }
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

private fun formatRelativeTime(rawTimestamp: String?): String {
    val createdAt = parseTimestamp(rawTimestamp) ?: return "Just now"
    val now = Instant.now()
    val diff = Duration.between(createdAt, now).seconds.coerceAtLeast(0)

    return when {
        diff < 60 -> "Just now"
        diff < 3600 -> "${diff / 60}m ago"
        diff < 86_400 -> "${diff / 3600}h ago"
        diff < 604_800 -> "${diff / 86_400}d ago"
        else -> DateTimeFormatter.ofPattern("MMM d").withZone(ZoneId.systemDefault()).format(createdAt)
    }
}

private fun parseTimestamp(rawTimestamp: String?): Instant? {
    if (rawTimestamp.isNullOrBlank()) return null

    return try {
        Instant.parse(rawTimestamp)
    } catch (_: Exception) {
        try {
            OffsetDateTime.parse(rawTimestamp).toInstant()
        } catch (_: Exception) {
            try {
                LocalDateTime.parse(rawTimestamp).toInstant(ZoneOffset.UTC)
            } catch (_: Exception) {
                null
            }
        }
    }
}
