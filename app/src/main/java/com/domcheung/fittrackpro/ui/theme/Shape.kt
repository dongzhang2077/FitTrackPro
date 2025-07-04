package com.domcheung.fittrackpro.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Hand-drawn style irregular corner shapes
val FitTrackShapes = Shapes(
    // Small components - buttons, chips, small cards
    small = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 10.dp,
        bottomStart = 6.dp,
        bottomEnd = 12.dp
    ),

    // Medium components - cards, input fields, dialogs
    medium = RoundedCornerShape(
        topStart = 14.dp,
        topEnd = 16.dp,
        bottomStart = 12.dp,
        bottomEnd = 18.dp
    ),

    // Large components - bottom sheets, large modals
    large = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 24.dp,
        bottomStart = 18.dp,
        bottomEnd = 22.dp
    )
)

// Additional hand-drawn shape variants for variety
object HandDrawnShapes {
    // Slightly rotated variants for cards
    val cardVariant1 = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 12.dp,
        bottomStart = 18.dp,
        bottomEnd = 14.dp
    )

    val cardVariant2 = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 18.dp,
        bottomStart = 14.dp,
        bottomEnd = 16.dp
    )

    // Button variants for different states
    val buttonDefault = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 12.dp,
        bottomStart = 10.dp,
        bottomEnd = 6.dp
    )

    val buttonPressed = RoundedCornerShape(
        topStart = 6.dp,
        topEnd = 10.dp,
        bottomStart = 12.dp,
        bottomEnd = 8.dp
    )

    // Progress card with subtle irregularity
    val progressCard = RoundedCornerShape(
        topStart = 15.dp,
        topEnd = 17.dp,
        bottomStart = 13.dp,
        bottomEnd = 19.dp
    )

    // Action card for quick actions
    val actionCard = RoundedCornerShape(
        topStart = 13.dp,
        topEnd = 15.dp,
        bottomStart = 17.dp,
        bottomEnd = 11.dp
    )
}

// Regular shapes for components that need standard geometry
object StandardShapes {
    // Perfect circle for avatars and icons
    val circle = RoundedCornerShape(50)

    // Standard rounded rectangle for containers
    val container = RoundedCornerShape(16.dp)

    // Minimal rounding for input fields
    val input = RoundedCornerShape(8.dp)

    // No rounding for full-width elements
    val rectangle = RoundedCornerShape(0.dp)
}