package com.domcheung.fittrackpro.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domcheung.fittrackpro.presentation.model.MainTab
import com.domcheung.fittrackpro.presentation.model.TabItem
import com.domcheung.fittrackpro.presentation.model.TabDestinations

@Composable
fun FitTrackBottomNavigation(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabDestinations.tabs.forEach { tabItem ->
                if (tabItem.isSpecial) {
                    // Central prominent START button
                    CentralStartButton(
                        onClick = { onTabSelected(tabItem.tab) }
                    )
                } else {
                    // Regular tab item
                    RegularTabItem(
                        tabItem = tabItem,
                        isSelected = selectedTab == tabItem.tab,
                        onClick = { onTabSelected(tabItem.tab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CentralStartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(56.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape
            ),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Start Workout",
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun RegularTabItem(
    tabItem: TabItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = tabItem.icon,
            contentDescription = tabItem.label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}