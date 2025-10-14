package com.domcheung.fittrackpro.presentation.workout_session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.domcheung.fittrackpro.data.model.ExecutedExercise
import com.domcheung.fittrackpro.data.model.PersonalRecord
import com.domcheung.fittrackpro.data.model.RecordType
import com.domcheung.fittrackpro.data.model.WeightUnit
import com.domcheung.fittrackpro.data.model.WorkoutSession
import com.domcheung.fittrackpro.data.model.WorkoutStatus
import com.domcheung.fittrackpro.presentation.exercise_library.ExerciseLibraryScreen
import com.domcheung.fittrackpro.ui.theme.HandDrawnShapes
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    viewModel: WorkoutSessionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onWorkoutComplete: () -> Unit = {},
    onExerciseInfoClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()

    LaunchedEffect(uiState.workoutCompleted) {
        if (uiState.workoutCompleted) {
            onWorkoutComplete()
        }
    }
    LaunchedEffect(uiState.workoutAbandoned) {
        if (uiState.workoutAbandoned) {
            onNavigateBack()
        }
    }

    if (uiState.showExerciseLibrary) {
        ModalBottomSheet(onDismissRequest = { viewModel.hideExerciseLibrary() }) {
            ExerciseLibraryScreen(
                onClose = { viewModel.hideExerciseLibrary() },
                onAddExercises = {
                    viewModel.addExercisesToCurrentSession(it)
                    viewModel.hideExerciseLibrary()
                },
                onExerciseClick = { onExerciseInfoClick(it) }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold { paddingValues ->
            if (currentSession == null && uiState.isLoading) {
                LoadingScreen()
            } else if (currentSession != null) {
                WorkoutSessionContent(
                    paddingValues = paddingValues,
                    session = currentSession!!,
                    uiState = uiState,
                    viewModel = viewModel,
                    onExerciseInfoClick = onExerciseInfoClick
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.isCurrentlyResting,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            RestScreen(
                remainingTime = uiState.restTimeRemaining,
                totalRestTime = uiState.totalRestTime,
                onSkipRest = { viewModel.skipRest() },
                onAdjustTime = { adjustment -> viewModel.adjustRestTime(adjustment) }
            )
        }

        PrNotificationOverlay(
            records = uiState.newlyAchievedRecords,
            onFinished = {
                viewModel.clearNewPrNotifications()
            }
        )

        if (uiState.showAbandonDialog) {
            AbandonWorkoutDialog(
                onConfirm = { viewModel.abandonWorkout() },
                onDismiss = { viewModel.hideAbandonDialog() }
            )
        }
        if (uiState.showCompleteDialog) {
            CompleteWorkoutDialog(
                session = currentSession,
                onConfirm = { viewModel.completeWorkout() },
                onDismiss = { viewModel.hideCompleteDialog() }
            )
        }
        if (uiState.showFinishWorkoutDialog) {
            FinishWorkoutConfirmationDialog(
                onConfirm = { viewModel.completeWorkout() },
                onDismiss = { viewModel.hideFinishWorkoutDialog() }
            )
        }
        if (uiState.showSettingsDialog) {
            WorkoutSettingsDialog(
                onDismiss = { viewModel.hideSettingsDialog() }
            )
        }

        if (uiState.showInvalidInputDialog) {
            uiState.inputErrorMessage?.let { message ->
                InvalidInputDialog(
                    errorMessage = message,
                    onDismiss = { viewModel.hideInvalidInputDialog() }
                )
            }
        }
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
    isPaused: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
                Text(
                    text = formatTime(elapsedTime),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { session.completionPercentage / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { if (isPaused) onResumeClick() else onPauseClick() }) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "Resume" else "Pause"
                    )
                }

                Button(
                    onClick = onStopClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop")
                }
            }
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
    onAddSet: () -> Unit,
    onRemoveSet: () -> Unit,
    isLoading: Boolean,
    weightUnit: WeightUnit,
    onToggleUnit: () -> Unit,
    onInfoClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                exercise.imageUrl?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Exercise Image",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = exercise.exerciseName, style = MaterialTheme.typography.titleLarge)
                    Text(text = "Set ${currentSetIndex + 1}", style = MaterialTheme.typography.bodyMedium)
                }

                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "View exercise details",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sets:",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(verticalAlignment = Alignment.CenterVertically)
                {
                    IconButton(
                        onClick = onRemoveSet,
                        enabled = !isLoading
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

            if (currentSetIndex < exercise.plannedSets.size) {
                val targetSet = exercise.plannedSets[currentSetIndex]
                Text(
                    text = "🎯 Target: ${targetSet.targetWeight}kg × ${targetSet.targetReps} reps",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val weightInCurrentUnit by remember(currentWeight, weightUnit) {
                    derivedStateOf {
                        val converted = if (weightUnit == WeightUnit.LB) {
                            currentWeight * 2.20462f
                        } else {
                            currentWeight
                        }
                        if (converted > 0) String.format("%.1f", converted) else ""
                    }
                }
                OutlinedTextField(
                    value = weightInCurrentUnit,
                    onValueChange = { newValue ->
                        val parsedValue = newValue.toFloatOrNull() ?: 0f
                        val weightInKg = if (weightUnit == WeightUnit.LB) {
                            parsedValue / 2.20462f
                        } else {
                            parsedValue
                        }
                        onWeightChange(weightInKg)
                    },
                    label = { Text("Weight") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    trailingIcon = {
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onToggleUnit() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = weightUnit.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Toggle weight unit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                Button(onClick = onCompleteSet, modifier = Modifier.weight(2f)) {
                    Icon(imageVector = Icons.Rounded.Check, contentDescription = "Complete Set")
                }

                OutlinedButton(onClick = onSkipSet, modifier = Modifier.weight(1f)) {
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Skip Set")
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

        Box(
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { if (totalRestTime > 0) (totalRestTime - remainingTime).toFloat() / totalRestTime else 0f },
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedButton(onClick = { onAdjustTime(-30) }) {
                Text("-30")
            }
            OutlinedButton(onClick = { onAdjustTime(-10) }) {
                Text("-10")
            }
            OutlinedButton(onClick = { onAdjustTime(10) }) {
                Text("+10")
            }
            OutlinedButton(onClick = { onAdjustTime(30) }) {
                Text("+30")
            }
        }
        
        Text(
            text = "seconds",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

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
    exerciseNumber: Int,
    onInfoClick: () -> Unit,
    onExerciseSelected: () -> Unit
) {
    val completedSets = exercise.executedSets.count { it.isCompleted }
    val totalSets = exercise.plannedSets.size
    val progress = if (totalSets > 0) completedSets.toFloat() / totalSets.toFloat() else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.medium)
            .clickable { onExerciseSelected() },
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentExercise) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCurrentExercise) FontWeight.Bold else FontWeight.Medium
                    )

                    Text(
                        text = "Sets: $completedSets/$totalSets",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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

                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "View exercise details",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (totalSets > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
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
    session: WorkoutSession?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (session == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Complete Workout") },
        text = {
            Column {
                Text("Great job! You've completed ${session.completionPercentage.toInt()}% of your workout.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Total volume: ${session.totalVolume.toInt()} kg")
                Spacer(modifier = Modifier.height(8.dp))
                if (session.isPlanModified) {
                    Text("Do you want to save the changes to your workout plan?")
                } else {
                    Text("Mark this workout as complete?")
                }
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

@Composable
fun BoxScope.PrNotificationOverlay(
    records: List<PersonalRecord>,
    onFinished: () -> Unit
) {
    val visibleNotifications = remember { mutableStateListOf<PersonalRecord>() }

    LaunchedEffect(records) {
        if (records.isNotEmpty()) {
            // Add records sequentially with delays
            records.forEachIndexed { index, record ->
                visibleNotifications.add(record)
                if (index < records.size - 1) {
                    delay(1000) // 1 second delay between appearances
                }
            }
            // Wait for all notifications to finish before calling onFinished
            delay(6000) // Wait 6 seconds after last appearance
            onFinished()
        }
    }

    visibleNotifications.forEach { record ->
        key(record.id) {
            LaunchedEffect(Unit) {
                // Keep each notification visible for 5 seconds
                delay(5000)
                visibleNotifications.remove(record)
            }
        }
    }

    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 90.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
    ) {
        visibleNotifications.forEach { record ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
            ) {
                PrNotificationCard(record = record)
            }
        }
    }
}

@Composable
private fun PrNotificationCard(record: PersonalRecord) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Celebration,
                contentDescription = "New Record",
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "🎉 New Personal Record!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                val recordText = when (record.recordType) {
                    RecordType.MAX_WEIGHT -> "Max Weight: ${record.weight} kg"
                    RecordType.MAX_REPS -> "Max Reps: ${record.reps}"
                    RecordType.MAX_VOLUME -> "Max Volume: ${record.volume} kg"
                    RecordType.MAX_ONE_REP_MAX -> "Max 1RM: ${record.oneRepMax} kg"
                }
                Text(
                    text = "${record.exerciseName} - $recordText",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun WorkoutSessionContent(
    paddingValues: PaddingValues,
    session: WorkoutSession,
    uiState: WorkoutSessionState,
    viewModel: WorkoutSessionViewModel,
    onExerciseInfoClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WorkoutSessionHeader(
                session = session,
                elapsedTime = uiState.elapsedTime,
                onPauseClick = { viewModel.pauseWorkout() },
                onResumeClick = { viewModel.resumeWorkout() },
                onStopClick = { viewModel.showAbandonDialog() },
                isPaused = session.status == WorkoutStatus.PAUSED
            )
        }

        item {
            session.exercises.getOrNull(uiState.currentExerciseIndex)?.let { currentExercise ->
                CurrentExerciseCard(
                    exercise = currentExercise,
                    currentSetIndex = uiState.currentSetIndex,
                    currentWeight = uiState.currentWeight,
                    currentReps = uiState.currentReps,
                    onWeightChange = { weight -> viewModel.updateCurrentWeight(weight) },
                    onRepsChange = { reps -> viewModel.updateCurrentReps(reps) },
                    onCompleteSet = { viewModel.completeCurrentSet() },
                    onSkipSet = { viewModel.skipCurrentSet() },
                    onAddSet = { viewModel.addSetToCurrentExercise() },
                    onRemoveSet = { viewModel.removeSetFromCurrentExercise() },
                    isLoading = uiState.isLoading,
                    weightUnit = uiState.weightUnit,
                    onToggleUnit = { viewModel.toggleWeightUnit() },
                    onInfoClick = { onExerciseInfoClick(currentExercise.exerciseId) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 Workout Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                OutlinedButton(onClick = { viewModel.showExerciseLibrary() }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Exercise")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }
        }

        itemsIndexed(session.exercises) { index, exercise ->
            ExerciseOverviewCard(
                exercise = exercise,
                isCurrentExercise = index == uiState.currentExerciseIndex,
                exerciseNumber = index + 1,
                onInfoClick = { onExerciseInfoClick(exercise.exerciseId) },
                onExerciseSelected = { viewModel.onExerciseSelected(index) }
            )
        }

        item {
            AnimatedVisibility(
                visible = uiState.isAllSetsCompleted,
                enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                        slideInVertically(animationSpec = tween(durationMillis = 500)) { it / 2 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔥 All Sets Completed!",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FloatingActionButton(
                        onClick = { viewModel.showCompleteDialog() },
                        containerColor = Color(0xFF2E7D32),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
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
    }

    AnimatedVisibility(
        visible = uiState.isCurrentlyResting,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        RestScreen(
            remainingTime = uiState.restTimeRemaining,
            totalRestTime = uiState.totalRestTime,
            onSkipRest = { viewModel.skipRest() },
            onAdjustTime = { adjustment -> viewModel.adjustRestTime(adjustment) },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.98f))
                .clickable(enabled = false, onClick = {})
        )
    }
}

@Composable
private fun InvalidInputDialog(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invalid Input") },
        text = { Text(errorMessage) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}