package com.domcheung.fittrackpro.presentation.workout_session

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import com.domcheung.fittrackpro.data.model.*
import com.domcheung.fittrackpro.ui.theme.HandDrawnShapes
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.rounded.Check

@Composable
fun WorkoutSessionScreen(
    sessionId: String,
    viewModel: WorkoutSessionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onWorkoutComplete: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()

    // Initialize session
//    LaunchedEffect(sessionId) {
//        viewModel.loadWorkoutSession(sessionId)
//    }

    // Handle completion
    LaunchedEffect(uiState.workoutCompleted) {
        if (uiState.workoutCompleted) {
            onWorkoutComplete()
        }
    }

    // Handle back navigation
    LaunchedEffect(uiState.workoutAbandoned) {
        if (uiState.workoutAbandoned) {
            onNavigateBack()
        }
    }

    // Show error snackbar
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            viewModel.clearError()
        }
    }

    if (currentSession == null) {
        LoadingScreen()
        return
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with progress and controls
        WorkoutSessionHeader(
            session = currentSession!!,
            elapsedTime = uiState.elapsedTime,
            onPauseClick = { viewModel.pauseWorkout() },
            onResumeClick = { viewModel.resumeWorkout() },
            onStopClick = { viewModel.showAbandonDialog() },
            isPaused = currentSession!!.status == WorkoutStatus.PAUSED,
            isLoading = uiState.isLoading
        )

        // Main content area
        AnimatedContent(
            targetState = uiState.isCurrentlyResting,
            transitionSpec = {
                slideInVertically { it } + fadeIn() togetherWith
                        slideOutVertically { -it } + fadeOut()
            },
            label = "main_content"
        ) { isResting ->
            if (isResting) {
                RestScreen(
                    remainingTime = uiState.restTimeRemaining,
                    totalRestTime = uiState.totalRestTime,
                    onSkipRest = { viewModel.skipRest() },
                    onAdjustTime = { adjustment -> viewModel.adjustRestTime(adjustment) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                WorkoutContent(
                    session = currentSession!!,
                    uiState = uiState,
                    onShowCompleteDialog = { viewModel.showCompleteDialog() },
                    currentExerciseIndex = uiState.currentExerciseIndex,
                    currentSetIndex = uiState.currentSetIndex,
                    onWeightChange = { weight -> viewModel.updateCurrentWeight(weight) },
                    onRepsChange = { reps -> viewModel.updateCurrentReps(reps) },
                    onCompleteSet = { viewModel.completeCurrentSet() },
                    onSkipSet = { viewModel.skipCurrentSet() },
                    onReplaceExercise = { viewModel.showReplaceExerciseDialog() },
                    onAddSet = { viewModel.addSetToCurrentExercise() },
                    onRemoveSet = { viewModel.removeSetFromCurrentExercise() },
                    currentWeight = uiState.currentWeight,
                    currentReps = uiState.currentReps,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Bottom action bar
        WorkoutBottomBar(
            onCompleteWorkout = { viewModel.showCompleteDialog() },
            onShowSettings = { viewModel.showSettingsDialog() },
            isCompleteEnabled = uiState.canCompleteWorkout,
            isLoading = uiState.isLoading
        )
    }

    // Dialogs
    if (uiState.showAbandonDialog) {
        AbandonWorkoutDialog(
            onConfirm = {
                viewModel.abandonWorkout()
                // The workoutAbandoned state will now trigger the onNavigateBack lambda
            },
            onDismiss = { viewModel.hideAbandonDialog() }
        )
    }

    if (uiState.showCompleteDialog) {
        CompleteWorkoutDialog(
            session = currentSession!!,
            onConfirm = { viewModel.completeWorkout() },
            onDismiss = { viewModel.hideCompleteDialog() }
        )
    }

    if (uiState.showSettingsDialog) {
        WorkoutSettingsDialog(
            onDismiss = { viewModel.hideSettingsDialog() }
        )
    }

    if (uiState.showFinishWorkoutDialog) {
        FinishWorkoutConfirmationDialog(
            onConfirm = {
                // User confirms to finish, call the main complete logic
                viewModel.completeWorkout()
            },
            onDismiss = {
                // User wants to continue, just hide the dialog
                viewModel.hideFinishWorkoutDialog()
            }
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading workout...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WorkoutSessionHeader(
    session: WorkoutSession,
    elapsedTime: Long,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStopClick: () -> Unit,
    isPaused: Boolean,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.progressCard),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title and progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.planName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${session.completionPercentage.toInt()}% complete",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Timer
                Text(
                    text = formatTime(elapsedTime),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = session.completionPercentage / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pause/Resume button
                Button(
                    onClick = if (isPaused) onResumeClick else onPauseClick,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPaused) "Resume" else "Pause")
                }

                // Stop button
                OutlinedButton(
                    onClick = onStopClick,
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop")
                }
            }
        }
    }
}

@Composable
private fun WorkoutContent(
    session: WorkoutSession,
    currentExerciseIndex: Int,
    currentSetIndex: Int,
    onWeightChange: (Float) -> Unit,
    onRepsChange: (Int) -> Unit,
    onCompleteSet: () -> Unit,
    onSkipSet: () -> Unit,
    onReplaceExercise: () -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: () -> Unit,
    currentWeight: Float,
    currentReps: Int,
    uiState: WorkoutSessionState,
    onShowCompleteDialog: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Current exercise card
        item {
            if (currentExerciseIndex < session.exercises.size) {
                val currentExercise = session.exercises[currentExerciseIndex]
                CurrentExerciseCard(
                    exercise = currentExercise,
                    currentSetIndex = currentSetIndex,
                    currentWeight = currentWeight,
                    currentReps = currentReps,
                    onWeightChange = onWeightChange,
                    onRepsChange = onRepsChange,
                    onCompleteSet = onCompleteSet,
                    onSkipSet = onSkipSet,
                    onReplaceExercise = onReplaceExercise,
                    onAddSet = onAddSet,
                    onRemoveSet = onRemoveSet,
                    isLoading = isLoading
                )
            }
        }

        // Exercise list overview
        item {
            Text(
                text = "📋 Workout Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        itemsIndexed(session.exercises) { index, exercise ->
            ExerciseOverviewCard(
                exercise = exercise,
                isCurrentExercise = index == currentExerciseIndex,
                exerciseNumber = index + 1
            )
        }

        item {
            // This button will only appear when all sets are completed.
            AnimatedVisibility(
                visible = uiState.isAllSetsCompleted,
                enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                        slideInVertically(animationSpec = tween(durationMillis = 500)) { it / 2 }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔥 All Sets Completed!",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    // The big green checkmark button
                    FloatingActionButton(
                        onClick = onShowCompleteDialog,
                        containerColor = Color(0xFF2E7D32), // A nice green color
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Complete Workout",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom bar
        }
    }
}

@Composable
private fun CurrentExerciseCard(
    exercise: ExecutedExercise,
    currentSetIndex: Int,
    currentWeight: Float,
    currentReps: Int,
    onWeightChange: (Float) -> Unit,
    onRepsChange: (Int) -> Unit,
    onCompleteSet: () -> Unit,
    onSkipSet: () -> Unit,
    onReplaceExercise: () -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.cardVariant1),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Exercise header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.exerciseName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Set ${currentSetIndex + 1} of ${exercise.plannedSets.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Replace exercise button
                IconButton(onClick = onReplaceExercise) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Replace exercise"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Set management
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sets:",
                    style = MaterialTheme.typography.titleMedium
                )

                Row {
                    IconButton(
                        onClick = onRemoveSet,
                        enabled = exercise.plannedSets.size > 1
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Remove set")
                    }

                    Text(
                        text = "${exercise.plannedSets.size}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(onClick = onAddSet) {
                        Icon(Icons.Default.Add, contentDescription = "Add set")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current set target
            if (currentSetIndex < exercise.plannedSets.size) {
                val targetSet = exercise.plannedSets[currentSetIndex]
                Text(
                    text = "🎯 Target: ${targetSet.targetWeight}kg × ${targetSet.targetReps} reps",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Input fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Weight input
                OutlinedTextField(
                    value = if (currentWeight > 0) currentWeight.toString() else "",
                    onValueChange = { value ->
                        value.toFloatOrNull()?.let { onWeightChange(it) }
                    },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                )

                // Reps input
                OutlinedTextField(
                    value = if (currentReps > 0) currentReps.toString() else "",
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { onRepsChange(it) }
                    },
                    label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCompleteSet,
                    enabled = !isLoading && currentWeight > 0 && currentReps > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Set")
                }

                OutlinedButton(
                    onClick = onSkipSet,
                    enabled = !isLoading
                ) {
                    Text("Skip")
                }
            }
        }
    }
}

@Composable
private fun RestScreen(
    remainingTime: Long,
    totalRestTime: Long,
    onSkipRest: () -> Unit,
    onAdjustTime: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "💤 Rest Time",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Circular progress
        Box(
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = if (totalRestTime > 0) (totalRestTime - remainingTime).toFloat() / totalRestTime else 0f,
                modifier = Modifier.size(200.dp),
                strokeWidth = 8.dp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = formatTime(remainingTime),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Time adjustment buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = { onAdjustTime(-30) }) {
                Text("-30s")
            }
            OutlinedButton(onClick = { onAdjustTime(-10) }) {
                Text("-10s")
            }
            OutlinedButton(onClick = { onAdjustTime(10) }) {
                Text("+10s")
            }
            OutlinedButton(onClick = { onAdjustTime(30) }) {
                Text("+30s")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Skip rest button
        Button(
            onClick = onSkipRest,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Skip Rest")
        }
    }
}

@Composable
private fun ExerciseOverviewCard(
    exercise: ExecutedExercise,
    isCurrentExercise: Boolean,
    exerciseNumber: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.cardVariant2),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentExercise) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise number
            Surface(
                shape = CircleShape,
                color = if (isCurrentExercise) {
                    MaterialTheme.colorScheme.primary
                } else if (exercise.isCompleted) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (exercise.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = exerciseNumber.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentExercise) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Exercise info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isCurrentExercise) FontWeight.Bold else FontWeight.Medium
                )

                val completedSets = exercise.executedSets.count { it.isCompleted }
                val totalSets = exercise.plannedSets.size
                Text(
                    text = "Sets: $completedSets/$totalSets",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status indicator
            if (isCurrentExercise) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Current exercise",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else if (exercise.isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun WorkoutBottomBar(
    onCompleteWorkout: () -> Unit,
    onShowSettings: () -> Unit,
    isCompleteEnabled: Boolean,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onShowSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Settings")
            }

            Button(
                onClick = onCompleteWorkout,
                enabled = isCompleteEnabled && !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete Workout")
            }
        }
    }
}

@Composable
private fun AbandonWorkoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Abandon Workout?") },
        text = {
            Text("Are you sure you want to stop this workout? Your progress will be saved, but the workout will be marked as abandoned.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Abandon", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Continue Workout")
            }
        }
    )
}

@Composable
private fun CompleteWorkoutDialog(
    session: WorkoutSession,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Complete Workout") },
        text = {
            Column {
                Text("Great job! You've completed ${session.completionPercentage.toInt()}% of your workout.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Total volume: ${session.totalVolume.toInt()} kg")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Mark this workout as complete?")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Complete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Continue")
            }
        }
    )
}



@Composable
private fun WorkoutSettingsDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Workout Settings") },
        text = {
            Column {
                Text("Settings coming soon:")
                Text("• Default rest time")
                Text("• Weight unit (kg/lb)")
                Text("• Timer sounds")
                Text("• Auto-advance sets")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

// Utility function
private fun formatTime(timeInMillis: Long): String {
    val totalSeconds = timeInMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

@Composable
private fun FinishWorkoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Workout Complete!") },
        text = {
            Text("Congratulations! You've completed all planned exercises. Finish workout?")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Finish Workout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Continue Training")
            }
        }
    )
}