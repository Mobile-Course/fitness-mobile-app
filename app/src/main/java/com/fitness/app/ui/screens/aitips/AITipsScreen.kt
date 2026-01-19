package com.fitness.app.ui.screens.aitips

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
            // Hint: Create a card or text field where the user can ask questions
            Text(
                text = "Ask your AI Coach anything about your training or nutrition.",
                style = MaterialTheme.typography.bodyLarge,
                color = accentDark,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Example Button to trigger AI
            Button(
                onClick = { viewModel.getAITip() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = accentDark)
            ) {
                Text("Get Personalized Tip")
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
