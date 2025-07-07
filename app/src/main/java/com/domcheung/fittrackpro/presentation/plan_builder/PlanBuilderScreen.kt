package com.domcheung.fittrackpro.presentation.plan_builder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * The main screen for building and editing a workout plan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanBuilderScreen(
    viewModel: PlanBuilderViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // --- NEW: Collect state from ViewModel ---
    // This allows the UI to react to changes in the ViewModel's state.
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Plan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Implement Save logic */ }) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Save Plan"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { /* TODO: Implement Add Exercise logic */ },
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
            // --- Editable Plan Title ---
            EditablePlanTitle(
                name = uiState.planName,
                isEditing = uiState.isEditingName,
                onNameChange = viewModel::onPlanNameChanged,
                onToggleEdit = viewModel::onToggleEditName
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Conditional Content: Empty State or Exercise List ---
            if (uiState.exercises.isEmpty()) {
                EmptyPlanContent(
                    onAddExerciseClick = { /* TODO: Call FAB's logic */ }
                )
            } else {
                // TODO: Implement the exercise list here in the next step
                Text("Exercise list will go here.")
            }
        }
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