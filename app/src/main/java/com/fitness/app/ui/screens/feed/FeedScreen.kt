package com.fitness.app.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.fitness.app.ui.components.FitTrackHeader
import com.fitness.app.ui.components.PostItem
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(viewModel: FeedViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val uiState by viewModel.uiStateLiveData.observeAsState(FeedUiState())
    val posts = uiState.posts
    val isLoading = uiState.isLoading
    val error = uiState.error
    val likedPostIds = uiState.likedPostIds
    val currentUsername = uiState.currentUsername
    val listState = rememberLazyListState()

    // True only on the very first load (no posts yet and loading)
    val isInitialLoading = posts.isEmpty() && isLoading

    // Infinite scrolling logic
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
    LaunchedEffect(isScrollToEnd, isLoading) {
        if (isScrollToEnd && !isLoading) {
            viewModel.loadPosts()
        }
    }

    val bgColor = MaterialTheme.colorScheme.background
    val accentDark = MaterialTheme.colorScheme.onBackground

    Column(modifier = Modifier.fillMaxSize().background(bgColor)) {
        FitTrackHeader()

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

            // Full-screen spinner on initial load
            if (isInitialLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 3.dp
                )
            } else {
                val pullState = rememberPullToRefreshState()
                PullToRefreshBox(
                    isRefreshing = isLoading && posts.isNotEmpty(),
                    onRefresh = { viewModel.refresh() },
                    state = pullState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (posts.isEmpty() && !isLoading && error == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No posts yet. Be the first to share your workout!",
                                modifier = Modifier.padding(32.dp),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = accentDark,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(posts, key = { it.id }) { post ->
                                val isLikedByUser =
                                    (currentUsername != null && post.likes?.any { it.username == currentUsername } == true) ||
                                    (post.isLikedByMe) // Use the flag from normalized local DB
                                
                                PostItem(
                                    post = post,
                                    isLiked = isLikedByUser || likedPostIds.contains(post.id),
                                    onLikeClick = { viewModel.toggleLike(post.id) },
                                    onAddComment = { content ->
                                        viewModel.addComment(post.id, content)
                                    },
                                    onCommentsClick = {
                                        viewModel.fetchPostDetails(post.id)
                                    }
                                )
                            }

                            // Pagination spinner at the bottom
                            if (isLoading && posts.isNotEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (error != null && posts.isEmpty()) {
                Text(
                    text = error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }
        }
    }
}
