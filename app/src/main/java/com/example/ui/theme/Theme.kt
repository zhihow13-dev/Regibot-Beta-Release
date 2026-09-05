package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = ObsidianDeep,
    primaryContainer = ObsidianElevated,
    onPrimaryContainer = CyanGlow,
    secondary = AmberEnergy,
    onSecondary = ObsidianDeep,
    secondaryContainer = ObsidianCard,
    onSecondaryContainer = AmberPulse,
    tertiary = VioletEcho,
    background = ObsidianDeep,
    onBackground = TextPrimaryDark,
    surface = ObsidianSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = ObsidianBorder,
    error = CrimsonAlert,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = CyanMuted,
    onPrimary = Color.White,
    primaryContainer = CleanCard,
    onPrimaryContainer = ObsidianDeep,
    secondary = AmberEnergy,
    onSecondary = Color.Black,
    secondaryContainer = CleanCard,
    onSecondaryContainer = ObsidianDeep,
    tertiary = VioletEcho,
    background = CleanCanvas,
    onBackground = TextPrimaryLight,
    surface = CleanSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = CleanCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = CleanBorder,
    error = CrimsonAlert,
    onError = Color.White
)

@Composable
fun RegiBotTheme(
    darkTheme: Boolean = true, // Default to sleek obsidian dark mode for bot HUD
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Alias for compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    RegiBotTheme(darkTheme = darkTheme, content = content)
}

