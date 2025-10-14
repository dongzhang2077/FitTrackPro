package com.domcheung.fittrackpro.presentation.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

    // Default avatar options
    data class DefaultAvatar(
    val id: String,
    val name: String,
    val emoji: String,
    val color: Color,
    val description: String
)

    // Default avatar list
    val defaultAvatars = listOf(
    DefaultAvatar("avatar_1", "Strong", "💪", Color(0xFF4CAF50), "Power and strength"),
    DefaultAvatar("avatar_2", "Fit", "🏃", Color(0xFF2196F3), "Running and fitness"),
    DefaultAvatar("avatar_3", "Gym", "🏋️", Color(0xFFFF9800), "Weight training"),
    DefaultAvatar("avatar_4", "Healthy", "🥗", Color(0xFF4CAF50), "Healthy lifestyle"),
    DefaultAvatar("avatar_5", "Champion", "🏆", Color(0xFFFFD700), "Winner mindset"),
    DefaultAvatar("avatar_6", "Energy", "⚡", Color(0xFF9C27B0), "High energy"),
    DefaultAvatar("avatar_7", "Focused", "🎯", Color(0xFFE91E63), "Goal oriented"),
    DefaultAvatar("avatar_8", "Balanced", "🧘", Color(0xFF00BCD4), "Balance and mindfulness"),
    DefaultAvatar("avatar_9", "Athletic", "🤸", Color(0xFF8BC34A), "Athletic performance"),
    DefaultAvatar("avatar_10", "Determined", "💯", Color(0xFFFF5722), "100% effort"),
    DefaultAvatar("avatar_11", "Rising", "🌟", Color(0xFF673AB7), "Rising star"),
    DefaultAvatar("avatar_12", "Classic", "😊", Color(0xFF607D8B), "Simple and friendly")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarSelectionDialog(
    onDismiss: () -> Unit,
    onAvatarSelected: (DefaultAvatar) -> Unit,
    currentlySelected: DefaultAvatar? = null
) {
    var selectedAvatar by remember { mutableStateOf(currentlySelected) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Choose Your Avatar",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Select an avatar that represents your fitness journey!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(defaultAvatarOptions) { avatar ->
                        AvatarOption(
                            avatar = avatar,
                            isSelected = selectedAvatar?.id == avatar.id,
                            onSelected = {
                                selectedAvatar = it
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Skip")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            selectedAvatar?.let { onAvatarSelected(it) }
                        },
                        enabled = selectedAvatar != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Select")
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarOption(
    avatar: DefaultAvatar,
    isSelected: Boolean,
    onSelected: (DefaultAvatar) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected(avatar) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar emoji
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = avatar.color.copy(alpha = 0.2f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = avatar.emoji,
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = avatar.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = avatar.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}