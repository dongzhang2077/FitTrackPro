package com.domcheung.fittrackpro.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domcheung.fittrackpro.ui.theme.HandDrawnShapes

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onSignOut: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val workoutStatistics by viewModel.workoutStatistics.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    // Handle sign out completion
    LaunchedEffect(uiState.signOutCompleted) {
        if (uiState.signOutCompleted) {
            onSignOut()
            viewModel.clearEvents()
        }
    }

    // Show error snackbar
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // In a real app, you would show a SnackBar here
            viewModel.clearError()
        }
    }

    // Show sync completion message
    LaunchedEffect(uiState.syncCompleted) {
        if (uiState.syncCompleted) {
            // Could show a success toast here
            viewModel.clearEvents()
        }
    }

    if (!isLoggedIn) {
        // Show login required state
        NotLoggedInContent()
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // User profile header
        item {
            UserProfileHeader(
                userProfile = userProfile,
                isLoading = uiState.isLoading
            )
        }

        // Current goals card
        item {
            CurrentGoalsCard(
                userProfile = userProfile,
                goalProgress = viewModel.getGoalProgress(),
                isLoading = uiState.isLoading
            )
        }

        // Statistics card
        item {
            StatisticsCard(
                statistics = viewModel.getStatisticsSummary(),
                isLoading = uiState.isLoading
            )
        }

        // Settings sections
        item {
            SettingsSection(
                onSyncClick = { viewModel.syncData() },
                onSignOutClick = { viewModel.showSignOutDialog() },
                hasUnsyncedData = uiState.hasUnsyncedData,
                isSyncing = uiState.isSyncing,
                isSigningOut = uiState.isSigningOut
            )
        }
    }

    // Sign out confirmation dialog
    if (uiState.showSignOutDialog) {
        SignOutConfirmationDialog(
            onConfirm = {
                viewModel.hideSignOutDialog()
                viewModel.signOut()
            },
            onDismiss = { viewModel.hideSignOutDialog() }
        )
    }

    // Loading overlay
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun NotLoggedInContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Please Log In",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You need to be logged in to view your profile",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun UserProfileHeader(
    userProfile: UserProfileData,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.cardVariant1),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar placeholder
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                // Loading placeholders
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(24.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(16.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            } else {
                Text(
                    text = userProfile.name.ifEmpty { "User" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = userProfile.email.ifEmpty { "No email" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = userProfile.joinDate.ifEmpty { "Member since 2025" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CurrentGoalsCard(
    userProfile: UserProfileData,
    goalProgress: Float,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.progressCard),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🎯 Current Goals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                // Loading placeholders
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    if (it < 2) Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GoalStatItem(
                        label = "Current Weight",
                        value = userProfile.currentWeight,
                        color = MaterialTheme.colorScheme.primary
                    )

                    GoalStatItem(
                        label = "Target Weight",
                        value = userProfile.targetWeight,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    GoalStatItem(
                        label = "Height",
                        value = userProfile.height,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = goalProgress / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.tertiary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Goal Progress: ${goalProgress.toInt()}% complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatisticsCard(
    statistics: UserStatisticsSummary,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.cardVariant2),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Your Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                // Loading placeholders
                repeat(2) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp, 40.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                    }
                    if (row == 0) Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                // First row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        label = "Workouts",
                        value = "${statistics.totalWorkouts}",
                        color = MaterialTheme.colorScheme.primary
                    )

                    StatItem(
                        label = "Streak",
                        value = "${statistics.currentStreak}",
                        color = MaterialTheme.colorScheme.secondary
                    )

                    StatItem(
                        label = "Records",
                        value = "${statistics.totalPersonalRecords}",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Second row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        label = "Volume",
                        value = "${(statistics.totalVolumeLifted / 1000).toInt()}k kg",
                        color = MaterialTheme.colorScheme.primary
                    )

                    StatItem(
                        label = "Avg Duration",
                        value = "${(statistics.averageWorkoutDuration / 60000)}m",
                        color = MaterialTheme.colorScheme.secondary
                    )

                    StatItem(
                        label = "Since",
                        value = statistics.memberSince.substringAfter("since ").substringBefore(" 2025"),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalStatItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSection(
    onSyncClick: () -> Unit,
    onSignOutClick: () -> Unit,
    hasUnsyncedData: Boolean,
    isSyncing: Boolean,
    isSigningOut: Boolean
) {
    val settingsSections = listOf(
        "App Settings" to listOf(
            SettingItem(
                icon = Icons.Default.Help,
                title = "Help & Support",
                subtitle = "Get help and contact support",
                onClick = { /* TODO: Navigate to help */ }
            ),
            SettingItem(
                icon = Icons.Default.Info,
                title = "About FitTrack Pro",
                subtitle = "App version and information",
                onClick = { /* TODO: Show about dialog */ }
            )
        )
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        settingsSections.forEach { (sectionTitle, items) ->
            SettingsSectionCard(
                title = sectionTitle,
                items = items
            )
        }

        // Data sync card (if needed)
        if (hasUnsyncedData) {
            SyncDataCard(
                onSyncClick = onSyncClick,
                isSyncing = isSyncing
            )
        }

        // Sign out button (separate and prominent)
        SignOutCard(
            onSignOut = onSignOutClick,
            isSigningOut = isSigningOut
        )
    }

//    val settingsSections = listOf(
//        "Account Settings" to listOf(
//            SettingItem(
//                icon = Icons.Default.Edit,
//                title = "Edit Profile",
//                subtitle = "Update your personal information",
//                onClick = { /* TODO: Navigate to edit profile */ }
//            ),
//            SettingItem(
//                icon = Icons.Default.Lock,
//                title = "Change Password",
//                subtitle = "Update your account password",
//                onClick = { /* TODO: Navigate to change password */ }
//            ),
//            SettingItem(
//                icon = Icons.Default.Notifications,
//                title = "Notifications",
//                subtitle = "Manage workout reminders",
//                onClick = { /* TODO: Navigate to notification settings */ }
//            )
//        ),
//        "Fitness Settings" to listOf(
//            SettingItem(
//                icon = Icons.Default.TrackChanges,
//                title = "Goal Management",
//                subtitle = "Set and update your fitness goals",
//                onClick = { /* TODO: Navigate to goal settings */ }
//            ),
//            SettingItem(
//                icon = Icons.Default.Schedule,
//                title = "Workout Schedule",
//                subtitle = "Customize your training schedule",
//                onClick = { /* TODO: Navigate to schedule settings */ }
//            ),
//            SettingItem(
//                icon = Icons.Default.Analytics,
//                title = "Progress Tracking",
//                subtitle = "Configure measurement preferences",
//                onClick = { /* TODO: Navigate to tracking settings */ }
//            )
//        ),
//        "App Settings" to listOf(
//            SettingItem(
//                icon = Icons.Default.Palette,
//                title = "Theme Settings",
//                subtitle = "Choose your preferred theme",
//                onClick = { /* TODO: Navigate to theme settings */ }
//            ),
//            SettingItem(
//                icon = Icons.Default.Storage,
//                title = "Data Export",
//                subtitle = "Export your workout data",
//                onClick = { /* TODO: Implement data export */ }
//            ),
//            SettingItem(
//                icon = Icons.Default.Help,
//                title = "Help & Support",
//                subtitle = "Get help and contact support",
//                onClick = { /* TODO: Navigate to help */ }
//            ),
//            SettingItem(
//                icon = Icons.Default.Info,
//                title = "About FitTrack Pro",
//                subtitle = "App version and information",
//                onClick = { /* TODO: Show about dialog */ }
//            )
//        )
//    )
//
//    Column(
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        settingsSections.forEach { (sectionTitle, items) ->
//            SettingsSectionCard(
//                title = sectionTitle,
//                items = items
//            )
//        }
//
//        // Data sync card (if needed)
//        if (hasUnsyncedData) {
//            SyncDataCard(
//                onSyncClick = onSyncClick,
//                isSyncing = isSyncing
//            )
//        }
//
//        // Sign out button (separate and prominent)
//        SignOutCard(
//            onSignOut = onSignOutClick,
//            isSigningOut = isSigningOut
//        )
//    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    items: List<SettingItem>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.cardVariant2),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            items.forEachIndexed { index, item ->
                SettingItemRow(item = item)
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingItemRow(item: SettingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SyncDataCard(
    onSyncClick: () -> Unit,
    isSyncing: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.actionCard),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "💾 Unsynced Data",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "You have local data that hasn't been synced",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSyncClick,
                enabled = !isSyncing
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Sync")
                }
            }
        }
    }
}

@Composable
private fun SignOutCard(
    onSignOut: () -> Unit,
    isSigningOut: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.actionCard)
            .clickable { onSignOut() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "🚪 Sign Out",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Log out of your account",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }

            if (isSigningOut) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Sign out",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SignOutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sign Out") },
        text = { Text("Are you sure you want to sign out? Any unsynced data will remain on this device.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Sign Out", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Data class for settings items
data class SettingItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit = {}
)