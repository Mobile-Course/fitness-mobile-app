package com.fitness.app.ui.screens.ai_tips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.fitness.app.ui.components.QuickQuestionButton
import com.fitness.app.ui.components.TipCard
import com.fitness.app.ui.theme.InputTextBlack

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
                                focusedTextColor = InputTextBlack,
                                unfocusedTextColor = InputTextBlack
                            )
                        )

                        Button(
                            onClick = { viewModel.getAITip() },
                            enabled = uiState.query.isNotBlank() && !uiState.isGenerating && uiState.aiResponse == null,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(56.dp), // Adjust size to match text field height roughly
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentDark,
                                disabledContainerColor = Color(0xFFE2E8F0),
                                disabledContentColor = Color.Gray
                            )
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // AI Response Display (Moved here)
                    if (uiState.isGenerating) {
                        CircularProgressIndicator(color = accentDark, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }

                    uiState.aiResponse?.let { response ->
                         Text(
                            text = response,
                            modifier = Modifier.padding(top = 8.dp),
                            color = accentDark,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Today's Tips Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Today's Tips",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentDark
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            TipCard(
                icon = Icons.Default.FitnessCenter, // Placeholder icon
                title = "Progressive Overload",
                tag = "workout",
                description = "Increase your weights by 2.5-5% each week to continuously challenge your muscles and promote growth.",
                accentColor = accentDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            TipCard(
                icon = Icons.Default.Restaurant, // Placeholder icon, need to ensure import
                title = "Post-Workout Nutrition",
                tag = "nutrition",
                description = "Consume protein within 30-60 minutes after training to optimize muscle recovery and growth.",
                accentColor = accentDark
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Questions Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Quick Questions",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentDark
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val quickQuestions = listOf(
                "Best exercises for abs?",
                "How much protein do I need?",
                "Tips for better sleep",
                "How to stay motivated?"
            )

            quickQuestions.forEach { question ->
                QuickQuestionButton(
                    text = question,
                    onClick = {
                        viewModel.onQueryChanged(question)
                        viewModel.getAITip()
                    },
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hint: Display the AI response here
            // Hint: Display the AI response here - MOVED ABOVE
        }
    }
}

