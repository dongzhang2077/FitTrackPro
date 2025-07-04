package com.domcheung.fittrackpro.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// FitTrack Pro Typography System
val FitTrackTypography = Typography(
    // App title and main headings
    displayLarge = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold,
        color = FitTrackOnSurface,
        letterSpacing = (-0.5).sp
    ),

    // Page titles and section headers
    headlineMedium = TextStyle(
        fontSize = 20.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
        color = FitTrackOnSurface,
        letterSpacing = 0.sp
    ),

    // Card titles and important labels
    titleLarge = TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = FitTrackOnSurface,
        letterSpacing = 0.sp
    ),

    // Subtitle and medium importance text
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Medium,
        color = FitTrackOnSurface,
        letterSpacing = 0.1.sp
    ),

    // Regular content text
    bodyLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        color = FitTrackOnSurface,
        letterSpacing = 0.25.sp
    ),

    // Secondary content and descriptions
    bodyMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal,
        color = FitTrackOnSurfaceVariant,
        letterSpacing = 0.4.sp
    ),

    // Button text and call-to-action
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        color = FitTrackOnSurface,
        letterSpacing = 0.1.sp
    ),

    // Small labels and captions
    labelMedium = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        color = FitTrackOnSurfaceVariant,
        letterSpacing = 0.5.sp
    ),

    // Very small text like timestamps
    labelSmall = TextStyle(
        fontSize = 10.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Normal,
        color = FitTrackOnSurfaceVariant,
        letterSpacing = 0.5.sp
    )
)