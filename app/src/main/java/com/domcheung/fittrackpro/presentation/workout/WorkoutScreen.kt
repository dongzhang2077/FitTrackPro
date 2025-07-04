package com.domcheung.fittrackpro.presentation.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.domcheung.fittrackpro.ui.theme.HandDrawnShapes

// Sample workout plan data
data class WorkoutPlan(
    val id: String,
    val name: String,
    val description: String,
    val duration: String,
    val exerciseCount: Int,
    val isToday: Boolean = false,
    val isCompleted: Boolean = false
)

@Composable
fun WorkoutScreen() {
    val workoutPlans = listOf(
        WorkoutPlan(
            id = "1",
            name = "Upper Body Strength",
            description = "Focus on chest, shoulders, and arms",
            duration = "45 min",
            exerciseCount = 6,
            isToday = true
        ),
        WorkoutPlan(
            id = "2",
            name = "Lower Body Power",
            description = "Legs and glutes workout",
            duration = "40 min",
            exerciseCount = 5
        ),
        WorkoutPlan(
            id = "3",
            name = "Full Body Circuit",
            description = "Complete body workout",
            duration = "50 min",
            exerciseCount = 8,
            isCompleted = true
        ),
        WorkoutPlan(
            id = "4",
            name = "Core & Cardio",
            description = "Abs and cardiovascular training",
            duration = "30 min",
            exerciseCount = 4
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Header
        WorkoutHeader()

        Spacer(modifier = Modifier.height(20.dp))

        // Workout plans list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(workoutPlans) { plan ->
                WorkoutPlanCard(workoutPlan = plan)
            }

            // Create new plan button
            item {
                CreateNewPlanCard()
            }
        }
    }
}

@Composable
private fun WorkoutHeader() {
    Column {
        Text(
            text = "💪 Workout Plans",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Choose your training for today",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WorkoutPlanCard(workoutPlan: WorkoutPlan) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.cardVariant1),
        colors = CardDefaults.cardColors(
            containerColor = when {
                workoutPlan.isToday -> MaterialTheme.colorScheme.primaryContainer
                workoutPlan.isCompleted -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            WorkoutStatusIcon(
                isToday = workoutPlan.isToday,
                isCompleted = workoutPlan.isCompleted
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Workout info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = workoutPlan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (workoutPlan.isToday) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge {
                            Text(
                                text = "TODAY",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                Text(
                    text = workoutPlan.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row {
                    WorkoutInfoChip(
                        icon = Icons.Default.Schedule,
                        text = workoutPlan.duration
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    WorkoutInfoChip(
                        icon = Icons.Default.FitnessCenter,
                        text = "${workoutPlan.exerciseCount} exercises"
                    )
                }
            }

            // Action button
            WorkoutActionButton(
                isToday = workoutPlan.isToday,
                isCompleted = workoutPlan.isCompleted
            )
        }
    }
}

@Composable
private fun WorkoutStatusIcon(
    isToday: Boolean,
    isCompleted: Boolean
) {
    val (icon, color) = when {
        isCompleted -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.tertiary
        isToday -> Icons.Default.Today to MaterialTheme.colorScheme.primary
        else -> Icons.Default.FitnessCenter to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(32.dp)
    )
}

@Composable
private fun WorkoutInfoChip(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WorkoutActionButton(
    isToday: Boolean,
    isCompleted: Boolean
) {
    val (text, icon, enabled) = when {
        isCompleted -> Triple("Done", Icons.Default.Check, false)
        isToday -> Triple("Start", Icons.Default.PlayArrow, true)
        else -> Triple("View", Icons.Default.Visibility, true)
    }

    FilledTonalButton(
        onClick = { /* TODO: Handle workout action */ },
        enabled = enabled,
        modifier = Modifier.clip(HandDrawnShapes.buttonDefault)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text)
    }
}

@Composable
private fun CreateNewPlanCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.cardVariant2),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "➕ Create New Plan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = "Design your custom workout",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}