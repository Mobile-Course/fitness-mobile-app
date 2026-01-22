package com.fitness.app.ui.screens.ai_tips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Composable for the AI Tips Page.
 * Design a conversational or dashboard-like UI for AI interactions here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AITipsScreen(
    viewModel: AITipsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val bgColor = Color(0xFFF0F4F8)
    val accentDark = Color(0xFF343E4E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // TOP APP BAR
        TopAppBar(
            title = {
                Text(
                    text = "AI Fitness Tips",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentDark
                    )
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
        )

        Divider(color = Color(0xFFE2E8F0))

        // MAIN CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ask AI Coach Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Area
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                         // You could add an icon here if available, e.g., Icon(Icons.Default.Star, ...)
                        Text(
                            text = "✨ Ask AI Coach",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = accentDark
                            )
                        )
                    }

                    Text(
                        text = "Get personalized fitness advice powered by AI",
                        style = MaterialTheme.typography.bodyMedium,
                        color = accentDark.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.query,
                            onValueChange = { viewModel.onQueryChanged(it) },
                            placeholder = {
                                Text(
                                    text = "Ask about workouts, nutrition...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentDark,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                cursorColor = accentDark,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )

                        Button(
                            onClick = { viewModel.getAITip() },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(56.dp), // Adjust size to match text field height roughly
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9EA6B5)) // Greyish generic placeholder color or accentDark
                        ) {
                            Text(text = "➤", fontSize = 20.sp, color = Color.White)
                        }
                    }
                    
                    if (uiState.error != null) {
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Hint: Display the AI response here
            if (uiState.isGenerating) {
                CircularProgressIndicator(color = accentDark)
            }

            uiState.aiResponse?.let { response ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = response,
                        modifier = Modifier.padding(16.dp),
                        color = accentDark
                    )
                }
            }
        }
    }
}