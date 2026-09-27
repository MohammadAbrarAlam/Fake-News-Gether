package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = Color(0xFF002233),
    primaryContainer = Color(0xFF004D66),
    onPrimaryContainer = Color(0xFFB3F5FF),
    secondary = EmeraldGreen,
    onSecondary = Color(0xFF00391A),
    secondaryContainer = Color(0xFF005328),
    onSecondaryContainer = Color(0xFF86F8B6),
    tertiary = AmberWarning,
    onTertiary = Color(0xFF452B00),
    background = MapDarkBg,
    onBackground = TextPrimary,
    surface = MapSurface,
    onSurface = TextPrimary,
    surfaceVariant = MapSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = MapSurfaceBorder,
    error = RedAlert,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme( // Keep cohesive high-contrast dark aesthetic
    primary = CyanNeon,
    onPrimary = Color(0xFF002233),
    secondary = EmeraldGreen,
    tertiary = AmberWarning,
    background = MapDarkBg,
    surface = MapSurface,
    surfaceVariant = MapSurfaceVariant,
    outline = MapSurfaceBorder
)

@Composable
fun MapPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
