package com.domcheung.fittrackpro.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domcheung.fittrackpro.ui.theme.HandDrawnShapes

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToWorkout: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToWorkoutSession: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeWorkoutSession by viewModel.activeWorkoutSession.collectAsState()
    val workoutStatistics by viewModel.workoutStatistics.collectAsState()

    // Get user display name
    val displayName = viewModel.getUserDisplayName()

    // Handle one-time events
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

    // Show error snackbar
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // In a real app, you would show a SnackBar here
            // For now, we'll just clear the error after showing it
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome header with real user data
        WelcomeHeader(
            userName = displayName,
            isLoading = uiState.isLoading
        )

        // Active workout card or today's goal card
        activeWorkoutSession?.let { session ->
            ActiveWorkoutCard(
                sessionData = session,
                onResumeClick = { viewModel.resumeActiveWorkout() },
                isLoading = uiState.isAnyOperationInProgress
            )
        } ?: run {
            TodayGoalCard(
                recommendedPlan = viewModel.getTodaysRecommendedPlan(),
                onStartClick = { viewModel.quickStartWorkout() },
                isLoading = uiState.isAnyOperationInProgress
            )
        }

        // Quick actions grid with real functionality
        QuickActionsGrid(
            onStartWorkoutClick = {
                activeWorkoutSession?.let {
                    viewModel.resumeActiveWorkout()
                } ?: run {
                    viewModel.quickStartWorkout()
                }
            },
            onViewProgressClick = onNavigateToProgress,
            isLoading = uiState.isAnyOperationInProgress,
            hasActiveWorkout = activeWorkoutSession != null
        )

        // Weekly overview with real statistics
        WeeklyOverviewCard(
            weeklyProgress = viewModel.getWeeklyProgress(),
            currentStreak = viewModel.getCurrentStreak(),
            thisWeekWorkouts = viewModel.getThisWeekWorkoutCount(),
            weeklyGoal = viewModel.getWeeklyGoal(),
            isLoading = uiState.isLoading
        )

        // Sync status if needed
        if (uiState.hasUnsyncedData) {
            SyncStatusCard(
                onSyncClick = { viewModel.syncData() },
                isSyncing = uiState.isSyncing
            )
        }

        Spacer(modifier = Modifier.weight(1f))
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
private fun WelcomeHeader(
    userName: String,
    isLoading: Boolean
) {
    Column {
        if (isLoading) {
            // Loading placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        } else {
            Text(
                text = "👋 Hi, $userName!",
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
            .clip(HandDrawnShapes.cardVariant1),
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

            // Progress indicator
            LinearProgressIndicator(
                progress = sessionData.completionPercentage / 100f,
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
    recommendedPlan: com.domcheung.fittrackpro.data.model.WorkoutPlan?,
    onStartClick: () -> Unit,
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
            .clip(HandDrawnShapes.actionCard)
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
            Text(
                text = "📊 This Week",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                // Loading placeholders
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
                        label = "Streak",
                        value = "$currentStreak days",
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

@Composable
private fun SyncStatusCard(
    onSyncClick: () -> Unit,
    isSyncing: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.cardVariant2),
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