package com.domcheung.fittrackpro.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest

    // High-quality avatar data class
    data class HighQualityAvatar(
    val id: String,
    val name: String,
    val resourceType: AvatarResourceType,
    val description: String,
    val category: AvatarCategory
)

    // Avatar resource types
    enum class AvatarResourceType {
        LOCAL_DRAWABLE,  // Local drawable resource
        LOCAL_ASSET,     // Local asset resource
        REMOTE_URL       // Remote URL
    }

    // Avatar categories
    enum class AvatarCategory {
        FITNESS,         // Fitness related
        SPORTS,          // Sports related
        LIFESTYLE,       // Lifestyle
        PROFESSIONAL,    // Professional image
        FUN,             // Fun image
        ABSTRACT         // Abstract image
    }

    // High-quality avatar list
    val highQualityAvatars = listOf(
        // Fitness-related avatars
        HighQualityAvatar(
        id = "fitness_1",
        name = "Strength Athlete",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Powerful strength athlete",
        category = AvatarCategory.FITNESS
    ),
    HighQualityAvatar(
        id = "fitness_2",
        name = "Runner",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Dedicated runner",
        category = AvatarCategory.FITNESS
    ),
    HighQualityAvatar(
        id = "fitness_3",
        name = "Yoga Master",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Zen yoga practitioner",
        category = AvatarCategory.FITNESS
    ),
    HighQualityAvatar(
        id = "fitness_4",
        name = "CrossFit Athlete",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "CrossFit enthusiast",
        category = AvatarCategory.FITNESS
        ),

        // Sports-related avatars
        HighQualityAvatar(
        id = "sports_1",
        name = "Basketball Player",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Basketball lover",
        category = AvatarCategory.SPORTS
    ),
    HighQualityAvatar(
        id = "sports_2",
        name = "Swimmer",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Professional swimmer",
        category = AvatarCategory.SPORTS
    ),
    HighQualityAvatar(
        id = "sports_3",
        name = "Cyclist",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Cycling enthusiast",
        category = AvatarCategory.SPORTS
    ),
    HighQualityAvatar(
        id = "sports_4",
        name = "Tennis Player",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Tennis champion",
        category = AvatarCategory.SPORTS
        ),

        // Lifestyle avatars
        HighQualityAvatar(
        id = "lifestyle_1",
        name = "Healthy Lifestyle",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Healthy living advocate",
        category = AvatarCategory.LIFESTYLE
    ),
    HighQualityAvatar(
        id = "lifestyle_2",
        name = "Wellness Coach",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Wellness and balance",
        category = AvatarCategory.LIFESTYLE
    ),
    HighQualityAvatar(
        id = "lifestyle_3",
        name = "Nutrition Expert",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Healthy eating advocate",
        category = AvatarCategory.LIFESTYLE
    ),
    HighQualityAvatar(
        id = "lifestyle_4",
        name = "Mindfulness",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Mindful practitioner",
        category = AvatarCategory.LIFESTYLE
        ),

        // Professional image avatars
        HighQualityAvatar(
        id = "professional_1",
        name = "Personal Trainer",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Certified trainer",
        category = AvatarCategory.PROFESSIONAL
    ),
    HighQualityAvatar(
        id = "professional_2",
        name = "Fitness Coach",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Professional coach",
        category = AvatarCategory.PROFESSIONAL
    ),
    HighQualityAvatar(
        id = "professional_3",
        name = "Athlete",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Professional athlete",
        category = AvatarCategory.PROFESSIONAL
    ),
    HighQualityAvatar(
        id = "professional_4",
        name = "Motivator",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Fitness motivator",
        category = AvatarCategory.PROFESSIONAL
        ),

        // Fun image avatars
        HighQualityAvatar(
        id = "fun_1",
        name = "Energy Boost",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Full of energy",
        category = AvatarCategory.FUN
    ),
    HighQualityAvatar(
        id = "fun_2",
        name = "Champion Mindset",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Winner attitude",
        category = AvatarCategory.FUN
    ),
    HighQualityAvatar(
        id = "fun_3",
        name = "Team Player",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Great team player",
        category = AvatarCategory.FUN
    ),
    HighQualityAvatar(
        id = "fun_4",
        name = "Adventure Seeker",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Always seeking adventure",
        category = AvatarCategory.FUN
        ),

        // Abstract image avatars
        HighQualityAvatar(
        id = "abstract_1",
        name = "Minimalist",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Simple and clean",
        category = AvatarCategory.ABSTRACT
    ),
    HighQualityAvatar(
        id = "abstract_2",
        name = "Modern",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Modern style",
        category = AvatarCategory.ABSTRACT
    ),
    HighQualityAvatar(
        id = "abstract_3",
        name = "Elegant",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Elegant design",
        category = AvatarCategory.ABSTRACT
    ),
    HighQualityAvatar(
        id = "abstract_4",
        name = "Dynamic",
        resourceType = AvatarResourceType.LOCAL_DRAWABLE,
        description = "Dynamic personality",
        category = AvatarCategory.ABSTRACT
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighQualityAvatarSelectionDialog(
    onDismiss: () -> Unit,
    onAvatarSelected: (HighQualityAvatar) -> Unit,
    currentlySelected: HighQualityAvatar? = null
) {
    var selectedCategory by remember { mutableStateOf<AvatarCategory?>(null) }
    var selectedAvatar by remember { mutableStateOf(currentlySelected) }

    // Filter avatars by selected category
    val filteredAvatars = selectedCategory?.let { category ->
        highQualityAvatars.filter { it.category == category }
    } ?: highQualityAvatars

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
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
                    text = "Select a high-quality avatar that represents your fitness journey!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Category filter
                CategoryFilterRow(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar grid
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredAvatars.chunked(2)) { avatarPair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            avatarPair.forEach { avatar ->
                                HighQualityAvatarOption(
                                    avatar = avatar,
                                    isSelected = selectedAvatar?.id == avatar.id,
                                    onSelected = { selectedAvatar = it },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
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
private fun CategoryFilterRow(
    selectedCategory: AvatarCategory?,
    onCategorySelected: (AvatarCategory?) -> Unit
) {
    val categories = listOf<AvatarCategory?>(null) + AvatarCategory.values()

    LazyColumn(
        modifier = Modifier.height(40.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(categories.chunked(4)) { categoryRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categoryRow.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        label = {
                            Text(
                                text = category?.name?.lowercase()?.replace("_", " ")
                                    ?.replaceFirstChar { it.uppercase() }
                                    ?: "All",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HighQualityAvatarOption(
    avatar: HighQualityAvatar,
    isSelected: Boolean,
    onSelected: (HighQualityAvatar) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 使用高质量头像图片组件
            HighQualityAvatarImage(
                avatar = avatar,
                modifier = Modifier.size(60.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = avatar.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )

            if (isSelected) {
                Surface(
                    modifier = Modifier.size(16.dp),
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

    // Get corresponding emoji placeholder based on category
    private fun getAvatarEmoji(category: AvatarCategory): String {
    return when (category) {
        AvatarCategory.FITNESS -> "💪"
        AvatarCategory.SPORTS -> "⚽"
        AvatarCategory.LIFESTYLE -> "🌿"
        AvatarCategory.PROFESSIONAL -> "👔"
        AvatarCategory.FUN -> "🎉"
        AvatarCategory.ABSTRACT -> "🎨"
    }
}