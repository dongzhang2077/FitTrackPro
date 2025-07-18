package com.domcheung.fittrackpro.presentation.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.launch

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = hiltViewModel(),
    onNavigateToWorkoutSession: (String) -> Unit = {},
    onNavigateToCreatePlan: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val userWorkoutPlans by viewModel.userWorkoutPlans.collectAsState()
    val activeWorkoutSession by viewModel.activeWorkoutSession.collectAsState()
    val filteredPlans by viewModel.getFilteredWorkoutPlans().collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle one-time navigation events
    LaunchedEffect(uiState.workoutStarted) {
        if (uiState.workoutStarted) {
            uiState.startedSessionId?.let { sessionId ->
                onNavigateToWorkoutSession(sessionId)
            }
            viewModel.clearEvents()
        }
    }

    // Show error snackbar
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            scope.launch {
                snackbarHostState.showSnackbar(errorMessage)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Use padding from Scaffold
                .padding(20.dp)
        ) {
            // Header with search
            WorkoutHeader(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { viewModel.searchWorkoutPlans(it) },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Active workout banner if exists
            activeWorkoutSession?.let { session ->
                ActiveWorkoutBanner(
                    session = session,
                    onResumeClick = {
                        onNavigateToWorkoutSession(session.id)
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Content based on state
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                filteredPlans.isEmpty() && uiState.searchQuery.isNotEmpty() -> {
                    EmptySearchContent(
                        searchQuery = uiState.searchQuery,
                        onClearSearch = { viewModel.searchWorkoutPlans("") }
                    )
                }
                userWorkoutPlans.isEmpty() -> {
                    EmptyPlansContent(
                        onCreatePlanClick = onNavigateToCreatePlan
                    )
                }
                else -> {
                    WorkoutPlansContent(
                        plans = filteredPlans,
                        onStartWorkout = { planId -> viewModel.startWorkout(planId) },
                        onCopyPlan = { planId, newName -> viewModel.copyWorkoutPlan(planId, newName) },
                        onDeletePlan = { plan -> viewModel.onDeletePlanClicked(plan) },
                        onCreatePlanClick = onNavigateToCreatePlan,
                        isLoading = uiState.isAnyOperationInProgress
                    )
                }
            }
        }
    }

    uiState.planToDelete?.let { plan ->
        DeleteConfirmationDialog(
            planName = plan.name,
            onConfirm = { viewModel.confirmDeletePlan() },
            onDismiss = { viewModel.cancelDeletePlan() }
        )
    }

}

@Composable
private fun WorkoutHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isLoading: Boolean
) {
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

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search workout plans") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true
        )
    }
}

@Composable
private fun ActiveWorkoutBanner(
    session: com.domcheung.fittrackpro.data.model.WorkoutSession,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.cardVariant1),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🏃 Active Workout",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "${session.planName} • ${session.completionPercentage.toInt()}% complete",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Button(
                onClick = onResumeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text("Resume")
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(HandDrawnShapes.cardVariant1),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySearchContent(
    searchQuery: String,
    onClearSearch: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No plans found for \"$searchQuery\"",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Try a different search term or create a new plan",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onClearSearch) {
            Text("Clear Search")
        }
    }
}

@Composable
private fun EmptyPlansContent(
    onCreatePlanClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Workout Plans Yet",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create your first workout plan to get started with your fitness journey!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCreatePlanClick,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Your First Plan")
        }
    }
}


@Composable
private fun WorkoutPlansContent(
    plans: List<WorkoutPlan>,
    onStartWorkout: (String) -> Unit,
    onCopyPlan: (String, String) -> Unit,
    onDeletePlan: (WorkoutPlan) -> Unit, // <<<--- 1. Add the new event handler parameter
    onCreatePlanClick: () -> Unit,
    isLoading: Boolean
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp) // Add padding to avoid FAB overlap
    ) {
        items(plans) { plan ->
            WorkoutPlanCard(
                plan = plan,
                onStartClick = { onStartWorkout(plan.id) },
                onCopyClick = { newName -> onCopyPlan(plan.id, newName) },
                onDeleteClick = { onDeletePlan(plan) }, // <<<--- 2. Pass the delete event down
                isLoading = isLoading
            )
        }

        // Create new plan button
        item {
            CreateNewPlanCard(
                onClick = onCreatePlanClick,
                isLoading = isLoading
            )
        }
    }
}

@Composable
private fun WorkoutPlanCard(
    plan: WorkoutPlan,
    onStartClick: () -> Unit,
    onCopyClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    isLoading: Boolean
) {
    var showCopyDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.cardVariant1),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Plan header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (plan.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = plan.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // More options menu
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Copy Plan") },
                            onClick = {
                                showMenu = false
                                showCopyDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Plan", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDeleteClick() // Call the new event handler
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Plan info chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlanInfoChip(
                    icon = Icons.Default.Schedule,
                    text = "${plan.estimatedDuration} min"
                )

                PlanInfoChip(
                    icon = Icons.Default.FitnessCenter,
                    text = "${plan.exercises.size} exercises"
                )

                if (plan.isTemplate) {
                    PlanInfoChip(
                        icon = Icons.Default.Bookmark,
                        text = "Template"
                    )
                }
            }

            // Target muscle groups
            if (plan.targetMuscleGroups.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Target: ${plan.targetMuscleGroups.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action button
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
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Workout")
            }
        }
    }

    // Copy dialog
    if (showCopyDialog) {
        CopyPlanDialog(
            originalName = plan.name,
            onConfirm = { newName ->
                onCopyClick(newName)
                showCopyDialog = false
            },
            onDismiss = { showCopyDialog = false }
        )
    }
}

@Composable
private fun PlanInfoChip(
    icon: ImageVector,
    text: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.clip(MaterialTheme.shapes.small)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CreateNewPlanCard(
    onClick: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.cardVariant2)
            .then(
                if (!isLoading) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
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

@Composable
private fun CopyPlanDialog(
    originalName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf("$originalName (Copy)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Copy Workout Plan") },
        text = {
            Column {
                Text("Enter a name for the copied plan:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Plan Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newName.isNotBlank()) {
                        onConfirm(newName.trim())
                    }
                },
                enabled = newName.isNotBlank()
            ) {
                Text("Copy")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * A confirmation dialog for deleting a workout plan.
 * @param planName The name of the plan to be deleted, shown in the dialog text.
 * @param onConfirm Lambda executed when the user confirms the deletion.
 * @param onDismiss Lambda executed when the user cancels or dismisses the dialog.
 */
@Composable
private fun DeleteConfirmationDialog(
    planName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Plan") },
        text = { Text("Are you sure you want to delete '$planName'? This action cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}