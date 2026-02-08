package com.fitness.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AppGradientStart,
    secondary = AppGradientEnd,
    background = AppBackground,
    surface = AppSurface,
    onPrimary = AppBackground,
    onSecondary = AppBackground,
    onBackground = AppOnBackground,
    onSurface = AppOnSurface,
    outline = AppOutline,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = AppGradientStart,
    secondary = AppGradientEnd,
    background = AppBackground,
    surface = AppSurface,
    onPrimary = AppBackground,
    onSecondary = AppBackground,
    onBackground = AppOnBackground,
    onSurface = AppOnSurface,
    outline = AppOutline,
    error = ErrorRed
)

@Composable
fun FitnessAppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
