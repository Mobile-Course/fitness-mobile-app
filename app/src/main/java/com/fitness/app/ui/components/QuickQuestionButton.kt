package com.fitness.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun QuickQuestionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentDark = Color(0xFF343E4E) // Matching AITipsScreen colors

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.wrapContentSize(),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, Color(0xFF94A3B8)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = accentDark,
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.wrapContentWidth(),
            textAlign = TextAlign.Start
        )
    }
}
