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

// Updated light theme color scheme based on the style guide
private val FitTrackLightColorScheme = lightColorScheme(
    primary = Color(0xFFD7819A),
    secondary = Color(0xFFC8AD7F),
    tertiary = Color(0xFFA8DADC),
    surface = Color(0xFFF8F9FA),
    background = Color(0xFFFFFFFF),
    onSurface = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFF666666),
    error = Color(0xFFF44336)
)

// Dark theme color scheme - for future implementation
private val FitTrackDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD7819A),
    secondary = Color(0xFFC8AD7F),
    tertiary = Color(0xFFA8DADC),
    surface = Color(0xFF2D2D2D),
    background = Color(0xFF121212),
    onSurface = Color(0xFFF8F9FA),
    onSurfaceVariant = Color(0xFFCCCCCC),
    error = Color(0xFFEF5350)
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
        shapes = HandDrawnShapes,
        content = content
    )
}