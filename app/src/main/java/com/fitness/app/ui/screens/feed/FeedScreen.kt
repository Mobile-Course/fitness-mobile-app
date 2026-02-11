package com.fitness.app.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitness.app.ui.components.FitTrackHeader
import com.fitness.app.ui.components.PostItem
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(viewModel: FeedViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val likedPostIds by viewModel.likedPostIds.collectAsState()
    val currentUsername by viewModel.currentUsername.collectAsState()
    val listState = rememberLazyListState()

    // Infinite scrolling logic
    val isScrollToEnd by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf false

            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= totalItems - 2 // Load more when 2 items from bottom
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { isScrollToEnd to isLoading }
                .distinctUntilChanged()
                .filter { (scrollToEnd, loading) -> scrollToEnd && !loading }
                .collect {
                    android.util.Log.d("FeedScreen", "Scroll to end detected. isLoading: $isLoading")
                    android.util.Log.d("FeedScreen", "Triggering loadPosts")
                    viewModel.loadPosts()
                }
    }

    val bgColor = MaterialTheme.colorScheme.background
    val accentDark = MaterialTheme.colorScheme.onBackground

    Column(modifier = Modifier.fillMaxSize().background(bgColor)) {
        // Minimized FitTrack branded header
        FitTrackHeader()

        // Content
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (posts.isEmpty() && !isLoading && error == null) {
                Text(
                        text = "No posts yet. Be the first to share your workout!",
                        modifier = Modifier.padding(32.dp),
                        style =
                                MaterialTheme.typography.bodyLarge.copy(
                                        color = accentDark,
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Center
                                )
                )
            } else {
                LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(posts, key = { it.id }) { post ->
                        val isLikedByUser =
                                currentUsername != null &&
                                        post.likes?.any { it.username == currentUsername } == true
                        PostItem(
                                post = post,
                                isLiked = isLikedByUser || likedPostIds.contains(post.id),
                                onLikeClick = { viewModel.toggleLike(post.id) },
                                onAddComment = { content ->
                                    viewModel.addComment(post.id, content)
                                }
                        )
                    }

                    if (isLoading) {
                        item {
                            Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }
                        }
                    }
                }
            }

            if (error != null && posts.isEmpty()) {
                Text(
                        text = error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                )
            }
        }
    }
}
