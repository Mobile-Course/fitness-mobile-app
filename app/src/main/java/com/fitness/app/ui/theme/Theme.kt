package com.fitness.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    secondary = NeonBlue,
    tertiary = SurfaceVariant,
    background = DarkGray,
    surface = DarkGray,
    onPrimary = DarkGray,
    onSecondary = DarkGray,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = NeonGreen,
    secondary = NeonBlue,
    tertiary = SurfaceVariant,
    background = DarkGray, // Keeping it dark for a "fitness" look even in light mode? Or maybe standard light?
    surface = DarkGray,   // Let's stick to a dark-first theme for fitness apps as they usually look more premium.
    onPrimary = DarkGray,
    onSecondary = DarkGray,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed
)

@Composable
fun FitnessAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
