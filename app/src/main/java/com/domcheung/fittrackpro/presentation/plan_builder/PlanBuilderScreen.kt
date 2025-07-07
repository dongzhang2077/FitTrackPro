package com.domcheung.fittrackpro.presentation.plan_builder

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domcheung.fittrackpro.data.model.PlannedExercise
import com.domcheung.fittrackpro.data.model.PlannedSet

/**
 * The main screen for building and editing a workout plan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanBuilderScreen(
    viewModel: PlanBuilderViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Plan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: viewModel.savePlan() */ }) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "Save Plan")
                    }
                }
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = {
                    // For testing, we add a sample exercise. Later this will open the Exercise Library.
                    viewModel.addExercise(com.domcheung.fittrackpro.data.model.Exercise(id = 1, name = "Test Exercise"))
                },
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Exercise",
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EditablePlanTitle(
                name = uiState.planName,
                isEditing = uiState.isEditingName,
                onNameChange = viewModel::onPlanNameChanged,
                onToggleEdit = viewModel::onToggleEditName
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

            if (uiState.exercises.isEmpty()) {
                EmptyPlanContent()
            } else {
                // The new exercise list UI
                PlanExerciseList(
                    exercises = uiState.exercises,
                    viewModel = viewModel // Pass viewModel to handle events
                )
            }
        }
    }
}

/**
 * Displays the list of exercises in the plan.
 */
@Composable
private fun PlanExerciseList(
    exercises: List<PlannedExercise>,
    viewModel: PlanBuilderViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        itemsIndexed(exercises, key = { _, item -> item.exerciseId }) { index, exercise ->
            PlanExerciseItem(
                exercise = exercise,
                onRemoveClick = { viewModel.removeExercise(index) },
                onAddSetClick = { viewModel.addSetToExercise(index) },
                onRemoveSetClick = { viewModel.removeSetFromExercise(index) },
                onSetChanged = { setIndex, updatedSet ->
                    viewModel.updateSet(index, setIndex, updatedSet)
                }
            )
        }
    }
}

/**
 * A single, expandable item in the exercise list.
 */
@Composable
private fun PlanExerciseItem(
    exercise: PlannedExercise,
    onRemoveClick: () -> Unit,
    onAddSetClick: () -> Unit,
    onRemoveSetClick: () -> Unit,
    onSetChanged: (Int, PlannedSet) -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header part of the card (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded } // Toggle expansion on click
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TODO: Replace with GIF placeholder
                Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = exercise.exerciseName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${exercise.sets.size} sets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRemoveClick) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Remove Exercise")
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand or collapse"
                )
            }

            // Expandable content
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // List of sets for this exercise
                    exercise.sets.forEachIndexed { setIndex, set ->
                        SetInputRow(
                            set = set,
                            onSetChanged = { updatedSet -> onSetChanged(setIndex, updatedSet) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Add/Remove Set buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        IconButton(onClick = onRemoveSetClick, enabled = exercise.sets.size > 1) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove last set")
                        }
                        IconButton(onClick = onAddSetClick) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Add a new set")
                        }
                    }
                }
            }
        }
    }
}

/**
 * A row for inputting the details of a single set (weight and reps).
 */
@Composable
private fun SetInputRow(
    set: PlannedSet,
    onSetChanged: (PlannedSet) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Set ${set.setNumber}", modifier = Modifier.width(50.dp))
        OutlinedTextField(
            value = if (set.targetWeight > 0) set.targetWeight.toString() else "",
            onValueChange = { onSetChanged(set.copy(targetWeight = it.toFloatOrNull() ?: 0f)) },
            label = { Text("Weight (lb)") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        OutlinedTextField(
            value = if (set.targetReps > 0) set.targetReps.toString() else "",
            onValueChange = { onSetChanged(set.copy(targetReps = it.toIntOrNull() ?: 0)) },
            label = { Text("Reps") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

/**
 * A composable for displaying and editing the plan title.
 * It switches between a Text and a TextField.
 */
@Composable
private fun EditablePlanTitle(
    name: String,
    isEditing: Boolean,
    onNameChange: (String) -> Unit,
    onToggleEdit: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isEditing) {
            // Show a TextField when in editing mode
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center),
                singleLine = true,
                label = { Text("Plan Name") }
            )
            IconButton(onClick = { onToggleEdit(false) }) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Confirm name")
            }
        } else {
            // Show the name as text with an edit icon
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            IconButton(onClick = { onToggleEdit(true) }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit name")
            }
        }
    }
}

/**
 * A composable that displays when the new plan has no exercises yet.
 * It prompts the user to add their first exercise.
 */
@Composable
private fun EmptyPlanContent(
    onAddExerciseClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PostAdd,
            contentDescription = "Add exercise",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your plan is empty",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the '+' button below to add your first exercise",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}