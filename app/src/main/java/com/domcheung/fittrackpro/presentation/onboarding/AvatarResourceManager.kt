package com.domcheung.fittrackpro.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * Avatar Resource Manager
 * Responsible for managing and loading different types of avatar resources
 */
object AvatarResourceManager {

    /**
     * Get avatar resource name
     */
    fun getAvatarResourceName(avatar: HighQualityAvatar): String {
        return when (avatar.resourceType) {
            AvatarResourceType.LOCAL_DRAWABLE -> {
                // Build drawable resource name
                "avatars/${avatar.category.name.lowercase()}/${avatar.id}"
            }
            AvatarResourceType.LOCAL_ASSET -> {
                // Build asset resource path
                "avatars/${avatar.category.name.lowercase()}/${avatar.id}.png"
            }
            AvatarResourceType.REMOTE_URL -> {
                // Remote URL, needs special handling
                avatar.id
            }
        }
    }

    /**
     * Get avatar placeholder emoji
     */
    fun getAvatarPlaceholder(avatar: HighQualityAvatar): String {
        return when (avatar.category) {
            AvatarCategory.FITNESS -> when (avatar.id) {
                "fitness_1" -> "💪"
                "fitness_2" -> "🏃"
                "fitness_3" -> "🧘"
                "fitness_4" -> "🏋️"
                else -> "💪"
            }
            AvatarCategory.SPORTS -> when (avatar.id) {
                "sports_1" -> "🏀"
                "sports_2" -> "🏊"
                "sports_3" -> "🚴"
                "sports_4" -> "🎾"
                else -> "⚽"
            }
            AvatarCategory.LIFESTYLE -> when (avatar.id) {
                "lifestyle_1" -> "🥗"
                "lifestyle_2" -> "🌿"
                "lifestyle_3" -> "🍏"
                "lifestyle_4" -> "🧘"
                else -> "🌿"
            }
            AvatarCategory.PROFESSIONAL -> when (avatar.id) {
                "professional_1" -> "👨‍⚕️"
                "professional_2" -> "👨‍🏫"
                "professional_3" -> "🏆"
                "professional_4" -> "🎯"
                else -> "👔"
            }
            AvatarCategory.FUN -> when (avatar.id) {
                "fun_1" -> "⚡"
                "fun_2" -> "🎉"
                "fun_3" -> "🤝"
                "fun_4" -> "🎪"
                else -> "🎉"
            }
            AvatarCategory.ABSTRACT -> when (avatar.id) {
                "abstract_1" -> "🎨"
                "abstract_2" -> "🔷"
                "abstract_3" -> "🔶"
                "abstract_4" -> "💫"
                else -> "🎨"
            }
        }
    }

    /**
     * Get avatar background color
     */
    fun getAvatarBackgroundColor(avatar: HighQualityAvatar): Color {
        return when (avatar.category) {
            AvatarCategory.FITNESS -> Color(0xFF4CAF50)  // Green
            AvatarCategory.SPORTS -> Color(0xFF2196F3)  // Blue
            AvatarCategory.LIFESTYLE -> Color(0xFF9C27B0)  // Purple
            AvatarCategory.PROFESSIONAL -> Color(0xFFFF9800)  // Orange
            AvatarCategory.FUN -> Color(0xFFE91E63)  // Pink
            AvatarCategory.ABSTRACT -> Color(0xFF607D8B)  // Blue Grey
        }
    }
}

/**
 * High-quality avatar display component
 * Supports loading and display of multiple resource types
 */
@Composable
fun HighQualityAvatarImage(
    avatar: HighQualityAvatar,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp
) {
    val context = LocalContext.current
    val resourceName = AvatarResourceManager.getAvatarResourceName(avatar)
    val placeholderEmoji = AvatarResourceManager.getAvatarPlaceholder(avatar)
    val backgroundColor = AvatarResourceManager.getAvatarBackgroundColor(avatar)

    when (avatar.resourceType) {
        AvatarResourceType.LOCAL_DRAWABLE -> {
            val drawableId = context.resources.getIdentifier(
                resourceName,
                "drawable",
                context.packageName
            )

            if (drawableId != 0) {
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = contentDescription ?: avatar.description,
                    modifier = modifier
                        .size(size)
                        .clip(CircleShape)
                        .background(
                            color = backgroundColor.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentScale = ContentScale.Crop
                )
            } else {
                AvatarPlaceholder(
                    emoji = placeholderEmoji,
                    backgroundColor = backgroundColor,
                    modifier = modifier.size(size)
                )
            }
        }

        AvatarResourceType.LOCAL_ASSET -> {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data("file:///android_asset/$resourceName")
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription ?: avatar.description,
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        color = backgroundColor.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentScale = ContentScale.Crop,
                loading = {
                    AvatarPlaceholder(
                        emoji = placeholderEmoji,
                        backgroundColor = backgroundColor,
                        modifier = modifier.size(size)
                    )
                },
                error = {
                    AvatarPlaceholder(
                        emoji = placeholderEmoji,
                        backgroundColor = backgroundColor,
                        modifier = modifier.size(size)
                    )
                }
            )
        }

        AvatarResourceType.REMOTE_URL -> {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resourceName)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription ?: avatar.description,
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        color = backgroundColor.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentScale = ContentScale.Crop,
                loading = {
                    AvatarPlaceholder(
                        emoji = placeholderEmoji,
                        backgroundColor = backgroundColor,
                        modifier = modifier.size(size)
                    )
                },
                error = {
                    AvatarPlaceholder(
                        emoji = placeholderEmoji,
                        backgroundColor = backgroundColor,
                        modifier = modifier.size(size)
                    )
                }
            )
        }
    }
}

/**
 * Avatar placeholder component
 */
@Composable
private fun AvatarPlaceholder(
    emoji: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = 0.2f)),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
            color = backgroundColor
        )
    }
}

/**
 * Default avatar list (for backward compatibility)
 */
val defaultAvatarOptions = highQualityAvatars.take(12).map { avatar ->
    DefaultAvatar(
        id = avatar.id,
        name = avatar.name,
        emoji = AvatarResourceManager.getAvatarPlaceholder(avatar),
        color = AvatarResourceManager.getAvatarBackgroundColor(avatar),
        description = avatar.description
    )
}
