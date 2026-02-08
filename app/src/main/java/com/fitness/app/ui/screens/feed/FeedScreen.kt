package com.fitness.app.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen() {
    val bgColor = MaterialTheme.colorScheme.background
    val accentDark = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // App Bar / Title
        TopAppBar(
            title = {
                Text(
                    text = "Feed",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentDark
                    )
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = bgColor
            )
        )

        Divider(color = MaterialTheme.colorScheme.outline)

        // Content
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
    }
}
