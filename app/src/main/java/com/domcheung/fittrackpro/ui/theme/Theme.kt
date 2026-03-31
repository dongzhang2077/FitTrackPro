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

private val FitTrackDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB2C7),
    onPrimary = Color(0xFF5A1D33),
    primaryContainer = Color(0xFF7A334B),
    onPrimaryContainer = Color(0xFFFFD9E4),

    secondary = Color(0xFFE7CC9A),
    onSecondary = Color(0xFF3E2E10),
    secondaryContainer = Color(0xFF5A4620),
    onSecondaryContainer = Color(0xFFFFE8BE),

    tertiary = Color(0xFFBCECF0),
    onTertiary = Color(0xFF00363B),
    tertiaryContainer = Color(0xFF1C4E54),
    onTertiaryContainer = Color(0xFFD3F8FC),

    background = Color(0xFF111318),
    onBackground = Color(0xFFE4E2E6),
    surface = Color(0xFF16181D),
    onSurface = Color(0xFFECE9EE),
    surfaceVariant = Color(0xFF46464F),
    onSurfaceVariant = Color(0xFFE3E1EA),
    outline = Color(0xFF9998A1),
    outlineVariant = Color(0xFF63636C),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FitTrackTypography,
        shapes = HandDrawnShapes,
        content = content
    )
}
