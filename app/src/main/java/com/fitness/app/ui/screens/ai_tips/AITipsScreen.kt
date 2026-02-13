package com.fitness.app.ui.screens.ai_tips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitness.app.ui.components.FitTrackHeader

@Composable
fun AITipsScreen(
    viewModel: AITipsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.sendInitialQuestionIfNeeded()
    }

    LaunchedEffect(uiState.messages.size, uiState.messages.lastOrNull()?.content) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        FitTrackHeader(
            actions = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = " Ask AI Coach",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )

        if (uiState.error != null) {
            Text(
                text = uiState.error ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val visibleMessages =
                uiState.messages.filterNot { message ->
                    message.role == ChatRole.ASSISTANT && message.content.isBlank()
                }

            items(visibleMessages, key = { it.id }) { message ->
                ChatBubble(message = message)
            }

            if (uiState.isStreaming) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFEFEFF7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Text(
                                    text = " Thinking...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChanged,
                modifier = Modifier.weight(1f).height(48.dp),
                placeholder = {
                    Text(
                        text = "Ask about workouts, nutrition, recovery...",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp)
                    )
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            IconButton(
                onClick = viewModel::sendCurrentQuery,
                enabled = uiState.query.isNotBlank() && !uiState.isStreaming
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (uiState.query.isNotBlank() && !uiState.isStreaming) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Gray
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == ChatRole.USER
    val isHebrew = containsHebrew(message.content)
    val formattedText = toBoldAnnotatedString(message.content)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isUser) Color(0xFF5A67D8) else Color(0xFFEFEFF7)
        ) {
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = formattedText,
                    color = if (isUser) Color.White else Color(0xFF111827),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            textDirection = if (isHebrew) TextDirection.Rtl else TextDirection.Ltr
                        )
                )
            }
        }
    }
}

private fun containsHebrew(text: String): Boolean {
    return text.any { it in '\u0590'..'\u05FF' }
}

private fun toBoldAnnotatedString(text: String): AnnotatedString {
    if (!text.contains("**")) return AnnotatedString(text)

    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val open = text.indexOf("**", cursor)
            if (open == -1) {
                append(text.substring(cursor))
                break
            }

            append(text.substring(cursor, open))
            val close = text.indexOf("**", open + 2)
            if (close == -1) {
                append(text.substring(open))
                break
            }

            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(text.substring(open + 2, close))
            }
            cursor = close + 2
        }
    }
}
