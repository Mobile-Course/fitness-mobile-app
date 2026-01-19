package com.fitness.app.ui.screens.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Composable representing the Post Creation Page.
 * Structure your UI here using Material 3 components.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    onPostCreated: () -> Unit,
    viewModel: PostViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val bgColor = Color(0xFFF0F4F8)
    val accentDark = Color(0xFF343E4E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // 1. TOP BAR: Standard across the app
        TopAppBar(
            title = {
                Text(
                    text = "Create Post",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentDark
                    )
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
        )

        Divider(color = Color(0xFFE2E8F0))

        // 2. CONTENT AREA: Add your text fields, image pickers, etc. here
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hint: Use OutlinedTextField for user input
            // OutlinedTextField(
            //     value = uiState.content,
            //     onValueChange = { viewModel.onContentChanged(it) },
            //     ...
            // )

            Spacer(modifier = Modifier.height(24.dp))

            // Hint: Add a button to submit the post
            Button(
                onClick = {
                    viewModel.submitPost(onSuccess = onPostCreated)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = accentDark)
            ) {
                Text("Share Workout")
            }
            
            // Helpful Tip: This is where you can show errors if content is empty
            uiState.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
