package com.domcheung.fittrackpro.presentation.home

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domcheung.fittrackpro.data.model.WorkoutPlan
import com.domcheung.fittrackpro.ui.theme.HandDrawnShapes

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToProgress: () -> Unit = {},
    onNavigateToWorkoutSession: (String) -> Unit = {},
    onNavigateToWorkoutTab: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeWorkoutSession by viewModel.activeWorkoutSession.collectAsState(initial = null)
    val displayName by viewModel.displayName.collectAsState()
    val storedWeeklyGoal by viewModel.weeklyWorkoutGoal.collectAsState()
    
    // State for weekly goal dialog
    var showWeeklyGoalDialog by remember { mutableStateOf(false) }
    var weeklyGoal by remember { mutableStateOf(storedWeeklyGoal) }

    LaunchedEffect(storedWeeklyGoal, showWeeklyGoalDialog) {
        if (!showWeeklyGoalDialog) {
            weeklyGoal = storedWeeklyGoal
        }
    }

    LaunchedEffect(uiState.workoutStarted) {
        if (uiState.workoutStarted) {
            uiState.startedSessionId?.let { sessionId ->
                onNavigateToWorkoutSession(sessionId)
            }
            viewModel.clearEvents()
        }
    }

    LaunchedEffect(uiState.workoutResumed) {
        if (uiState.workoutResumed) {
            uiState.resumedSessionId?.let { sessionId ->
                onNavigateToWorkoutSession(sessionId)
            }
            viewModel.clearEvents()
        }
    }

    LaunchedEffect(uiState.navigateToWorkoutTab) {
        if (uiState.navigateToWorkoutTab) {
            onNavigateToWorkoutTab()
            viewModel.clearEvents()
        }
    }

    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        WelcomeHeader(
            userName = displayName,
            isLoading = uiState.isLoading
        )

        val currentSession = activeWorkoutSession
        if (currentSession != null) {
            ActiveWorkoutCard(
                sessionData = currentSession,
                onResumeClick = { viewModel.resumeActiveWorkout() },
                isLoading = uiState.isAnyOperationInProgress
            )
        } else {
            val recommendedPlan by produceState<WorkoutPlan?>(initialValue = null) {
                value = viewModel.getTodaysRecommendedPlan()
            }
            TodayGoalCard(
                recommendedPlan = recommendedPlan,
                onStartClick = { viewModel.quickStartWorkout() },
                isLoading = uiState.isAnyOperationInProgress
            )
        }

        QuickActionsGrid(
            onStartWorkoutClick = {
                if (activeWorkoutSession != null) {
                    viewModel.resumeActiveWorkout()
                } else {
                    viewModel.quickStartWorkout()
                }
            },
            onViewProgressClick = onNavigateToProgress,
            isLoading = uiState.isAnyOperationInProgress,
            hasActiveWorkout = activeWorkoutSession != null
        )

        WeeklyOverviewCard(
            weeklyProgress = viewModel.getWeeklyProgress(),
            currentStreak = viewModel.getCurrentStreak(),
            thisWeekWorkouts = viewModel.getThisWeekWorkoutCount(),
            weeklyGoal = storedWeeklyGoal,
            isLoading = uiState.isLoading,
            onEditWeeklyGoal = {
                weeklyGoal = storedWeeklyGoal
                showWeeklyGoalDialog = true
            }
        )

        Spacer(modifier = Modifier.weight(1f))
    }

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
    
    // Weekly Goal Edit Dialog
    if (showWeeklyGoalDialog) {
        AlertDialog(
            onDismissRequest = { showWeeklyGoalDialog = false },
            title = { Text("Edit Weekly Workout Goal") },
            text = {
                Column {
                    Text("Set your weekly workout goal")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Modern slider-style input
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$weeklyGoal",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "times/week",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Slider
                        Slider(
                            value = weeklyGoal.toFloat(),
                            onValueChange = { weeklyGoal = it.toInt() },
                            valueRange = 1f..7f,
                            steps = 5, // 1-7 gives 5 steps between
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveWeeklyGoal(weeklyGoal)
                        showWeeklyGoalDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeeklyGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun WelcomeHeader(
    userName: String,
    isLoading: Boolean
) {
    Column {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        } else {
            Text(
                text = "👋 Hello, ${userName.ifBlank { "Friend" }}!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = "Ready for today's workout?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActiveWorkoutCard(
    sessionData: com.domcheung.fittrackpro.data.model.WorkoutSession,
    onResumeClick: () -> Unit,
    isLoading: Boolean
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
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🏋️ Active Workout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = sessionData.planName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { sessionData.completionPercentage / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Progress: ${sessionData.completionPercentage.toInt()}% completed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onResumeClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("▶️ Resume Workout")
            }
        }
    }
}

@Composable
private fun TodayGoalCard(
    recommendedPlan: WorkoutPlan?,
    onStartClick: () -> Unit,
    isLoading: Boolean
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🎯 Today's Recommendation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (recommendedPlan != null) {
                Text(
                    text = recommendedPlan.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                if (recommendedPlan.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recommendedPlan.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Estimated duration: ${recommendedPlan.estimatedDuration} minutes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onStartClick,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("🚀 Start Workout")
                }
            } else {
                Text(
                    text = "No workout plans available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Create your first workout plan to get started!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onStartWorkoutClick: () -> Unit,
    onViewProgressClick: () -> Unit,
    isLoading: Boolean,
    hasActiveWorkout: Boolean
) {
    Column {
        Text(
            text = "⚡ Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionItem(
                icon = if (hasActiveWorkout) Icons.Default.PlayArrow else Icons.Default.FitnessCenter,
                title = if (hasActiveWorkout) "Resume Workout" else "Start Workout",
                subtitle = if (hasActiveWorkout) "Continue training" else "Begin today's plan",
                onClick = onStartWorkoutClick,
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            )

            QuickActionItem(
                icon = Icons.Default.Analytics,
                title = "View Progress",
                subtitle = "Check your stats",
                onClick = onViewProgressClick,
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(HandDrawnShapes.small)
            .then(
                if (enabled) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WeeklyOverviewCard(
    weeklyProgress: Float,
    currentStreak: Int,
    thisWeekWorkouts: Int,
    weeklyGoal: Int,
    isLoading: Boolean,
    onEditWeeklyGoal: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.medium)
            .clickable { onEditWeeklyGoal() },
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
                Text(
                    text = "📊 This Week",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit weekly goal",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    if (it < 2) Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeeklyStatItem(
                        label = "Workouts",
                        value = "$thisWeekWorkouts/$weeklyGoal",
                        color = MaterialTheme.colorScheme.primary
                    )

                    WeeklyStatItem(
                        label = "Progress",
                        value = "${weeklyProgress.toInt()}%",
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    WeeklyStatItem(
                        label = "Streak (days)",
                        value = "$currentStreak",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyStatItem(
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
