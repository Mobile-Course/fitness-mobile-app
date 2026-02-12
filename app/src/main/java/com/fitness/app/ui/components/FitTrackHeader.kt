package com.fitness.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Minimized FitTrack branded header used across all screens.
 *
 * @param navigationIcon Optional composable for a back arrow or similar navigation icon.
 * @param actions Optional composable for trailing action icons (settings, send, etc.).
 */
@Composable
fun FitTrackHeader(
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    val brandColor = Color(0xFF3B3FC7) // deep indigo matching the icon in the picture
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val headerHeight = screenHeight * 0.08f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight),
        color = Color.White,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Optional leading navigation icon
            if (navigationIcon != null) {
                navigationIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Brand logo: dumbbell icon + "FitTrack" text
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = "FitTrack logo",
                tint = brandColor,
                modifier = Modifier
                    .size(22.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "FitTrack",
                color = Color(0xFF111827),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = (-0.3).sp
            )

            // Push actions to the end
            Spacer(modifier = Modifier.weight(1f))

            // Optional trailing actions
            if (actions != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    actions()
                }
            }
        }
    }
}
