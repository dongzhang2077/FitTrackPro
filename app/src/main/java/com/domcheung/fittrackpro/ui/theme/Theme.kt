package com.domcheung.fittrackpro.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light theme color scheme - our main FitTrack Pro theme
private val FitTrackLightColorScheme = lightColorScheme(
    // Primary colors
    primary = FitTrackPrimary,
    onPrimary = Color.White,
    primaryContainer = FitTrackPrimaryAlpha12,
    onPrimaryContainer = FitTrackOnSurface,

    // Secondary colors
    secondary = FitTrackSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0E6D6), // Light milk tea
    onSecondaryContainer = FitTrackOnSurface,

    // Tertiary colors
    tertiary = FitTrackTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8F4F5), // Light misty blue
    onTertiaryContainer = FitTrackOnSurface,

    // Background colors
    background = FitTrackBackground,
    onBackground = FitTrackOnSurface,

    // Surface colors
    surface = FitTrackSurface,
    onSurface = FitTrackOnSurface,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = FitTrackOnSurfaceVariant,

    // Error colors
    error = FitTrackError,
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),

    // Outline colors
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFF0F0F0)
)

// Dark theme color scheme - for future implementation
private val FitTrackDarkColorScheme = darkColorScheme(
    primary = FitTrackDarkPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF4A2C3A),
    onPrimaryContainer = FitTrackDarkOnSurface,

    secondary = Color(0xFFD4C4A0),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF4A4134),
    onSecondaryContainer = FitTrackDarkOnSurface,

    tertiary = Color(0xFFB8E6E8),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF2F4A4B),
    onTertiaryContainer = FitTrackDarkOnSurface,

    background = Color(0xFF121212),
    onBackground = FitTrackDarkOnSurface,

    surface = FitTrackDarkSurface,
    onSurface = FitTrackDarkOnSurface,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0B0B0),

    error = Color(0xFFEF5350),
    onError = Color.Black,
    errorContainer = Color(0xFF4E1D1D),
    onErrorContainer = Color(0xFFFFCDD2)
)

@Composable
fun FitTrackProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> FitTrackDarkColorScheme
        else -> FitTrackLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FitTrackTypography,
        shapes = FitTrackShapes,
        content = content
    )
}