package com.domcheung.fittrackpro.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.domcheung.fittrackpro.ui.theme.HandDrawnShapes

// Sample user data
data class UserProfile(
    val name: String,
    val email: String,
    val joinDate: String,
    val currentWeight: String,
    val targetWeight: String,
    val height: String
)

data class SettingItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit = {}
)

@Composable
fun ProfileScreen(
    onSignOut: () -> Unit = {}
) {
    val userProfile = UserProfile(
        name = "John Doe",
        email = "john.doe@example.com",
        joinDate = "Member since Jan 2025",
        currentWeight = "75 kg",
        targetWeight = "70 kg",
        height = "175 cm"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // User profile header
        item {
            UserProfileHeader(userProfile = userProfile)
        }

        // Current goals card
        item {
            CurrentGoalsCard(userProfile = userProfile)
        }

        // Settings sections
        item {
            SettingsSection(onSignOut = onSignOut)
        }
    }
}

@Composable
private fun UserProfileHeader(userProfile: UserProfile) {
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

            Text(
                text = userProfile.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = userProfile.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = userProfile.joinDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CurrentGoalsCard(userProfile: UserProfile) {
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
                progress = 0.8f,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Goal Progress: 80% complete (4kg to go)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
private fun SettingsSection(onSignOut: () -> Unit) {
    val settingsSections = listOf(
        "Account Settings" to listOf(
            SettingItem(
                icon = Icons.Default.Edit,
                title = "Edit Profile",
                subtitle = "Update your personal information"
            ),
            SettingItem(
                icon = Icons.Default.Lock,
                title = "Change Password",
                subtitle = "Update your account password"
            ),
            SettingItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Manage workout reminders"
            )
        ),
        "Fitness Settings" to listOf(
            SettingItem(
                icon = Icons.Default.TrackChanges,
                title = "Goal Management",
                subtitle = "Set and update your fitness goals"
            ),
            SettingItem(
                icon = Icons.Default.Schedule,
                title = "Workout Schedule",
                subtitle = "Customize your training schedule"
            ),
            SettingItem(
                icon = Icons.Default.Analytics,
                title = "Progress Tracking",
                subtitle = "Configure measurement preferences"
            )
        ),
        "App Settings" to listOf(
            SettingItem(
                icon = Icons.Default.Palette,
                title = "Theme Settings",
                subtitle = "Choose your preferred theme"
            ),
            SettingItem(
                icon = Icons.Default.Storage,
                title = "Data Export",
                subtitle = "Export your workout data"
            ),
            SettingItem(
                icon = Icons.Default.Help,
                title = "Help & Support",
                subtitle = "Get help and contact support"
            ),
            SettingItem(
                icon = Icons.Default.Info,
                title = "About FitTrack Pro",
                subtitle = "App version and information"
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

        // Sign out button (separate and prominent)
        SignOutCard(onSignOut = onSignOut)
    }
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
private fun SignOutCard(onSignOut: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.actionCard),
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

            TextButton(
                onClick = onSignOut,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Sign Out")
            }
        }
    }
}