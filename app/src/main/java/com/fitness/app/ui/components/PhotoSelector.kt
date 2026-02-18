package com.fitness.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PhotoSelector(
    onClick: () -> Unit,
    selectedImageUri: android.net.Uri? = null,
    existingImageUrl: String? = null,
    onRemoveClick: (() -> Unit)? = null,
    placeholderHeight: Dp = 56.dp,
    selectedImageHeight: Dp = 200.dp,
    modifier: Modifier = Modifier
) {
    if (selectedImageUri != null || (existingImageUrl != null && existingImageUrl.isNotBlank())) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(selectedImageHeight)
                .clickable(onClick = onClick)
                .border(
                    width = 1.dp,
                    color = Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(4.dp)
                )
                .clip(RoundedCornerShape(4.dp))
        ) {
            val resolvedUrl = if (selectedImageUri != null) {
                selectedImageUri.toString()
            } else {
                val url = existingImageUrl ?: ""
                if (url.startsWith("http")) url else "https://node86.cs.colman.ac.il$url"
            }
            PicassoImage(
                url = resolvedUrl,
                contentDescription = "Selected Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = android.widget.ImageView.ScaleType.CENTER_CROP
            )
            
            // Remove Button
            if (onRemoveClick != null) {
                androidx.compose.material3.IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Image",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .height(placeholderHeight),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFFF8FAFC),
                contentColor = Color(0xFF343E4E)
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = Color(0xFF343E4E)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Add Photo",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF343E4E)
                )
            )
        }
    }
}
