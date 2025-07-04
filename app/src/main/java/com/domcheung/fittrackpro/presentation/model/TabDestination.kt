package com.domcheung.fittrackpro.presentation.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Tab destinations for main navigation
 */
enum class MainTab {
    HOME,
    WORKOUT,
    START,      // Central prominent button
    PROGRESS,
    PROFILE
}

/**
 * Tab item configuration data class
 */
data class TabItem(
    val tab: MainTab,
    val icon: ImageVector,
    val label: String,
    val isSpecial: Boolean = false
)

/**
 * Tab destinations configuration
 */
object TabDestinations {
    val tabs = listOf(
        TabItem(MainTab.HOME, Icons.Default.Home, "Home"),
        TabItem(MainTab.WORKOUT, Icons.Default.FitnessCenter, "Workout"),
        TabItem(MainTab.START, Icons.Default.PlayArrow, "START", isSpecial = true),
        TabItem(MainTab.PROGRESS, Icons.Default.Analytics, "Progress"),
        TabItem(MainTab.PROFILE, Icons.Default.Person, "Profile")
    )
}