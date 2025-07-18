package com.domcheung.fittrackpro.presentation.exercise_library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domcheung.fittrackpro.data.model.Exercise
import androidx.compose.material.icons.filled.FitnessCenter

/**
 * The main screen for the Exercise Library, allowing users to browse, filter, and select exercises.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    viewModel: ExerciseLibraryViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onAddExercises: (Set<Int>) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredExercises by viewModel.filteredExercises.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Exercise") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        },
        bottomBar = {
            // The bottom bar shows the count of selected exercises and the button to add them.
            AddExercisesBottomBar(
                selectedCount = uiState.selectedExerciseIds.size,
                onAddClick = {
                    onAddExercises(uiState.selectedExerciseIds)
                    onClose() // Close the sheet after adding
                }
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- Primary Filter (Muscle Groups) ---
            MuscleGroupFilterList(
                muscleGroups = uiState.muscleGroupFilters,
                selectedMuscleGroup = uiState.selectedMuscleGroup,
                onMuscleGroupSelected = viewModel::onMuscleGroupSelected
            )

            // --- Main Content Area ---
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text("Search exercises...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )

                // Secondary Filter (Equipment)
                EquipmentFilterChips(
                    equipmentTypes = uiState.equipmentFilters,
                    selectedEquipment = uiState.selectedEquipment,
                    onEquipmentSelected = viewModel::onEquipmentSelected
                )

                // Exercise Grid
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    ExerciseGrid(
                        exercises = filteredExercises,
                        selectedExerciseIds = uiState.selectedExerciseIds,
                        onExerciseToggled = viewModel::onExerciseToggled
                    )
                }
            }
        }
    }
}

/**
 * Displays the vertical list of muscle groups for primary filtering.
 */
@Composable
private fun MuscleGroupFilterList(
    muscleGroups: List<String>,
    selectedMuscleGroup: String,
    onMuscleGroupSelected: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .width(100.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(muscleGroups) { group ->
            Text(
                text = group,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMuscleGroupSelected(group) }
                    .background(if (selectedMuscleGroup == group) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                textAlign = TextAlign.Center,
                fontWeight = if (selectedMuscleGroup == group) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedMuscleGroup == group) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Displays the horizontal, scrollable list of equipment filter chips.
 */
@Composable
private fun EquipmentFilterChips(
    equipmentTypes: List<String>,
    selectedEquipment: Set<String>,
    onEquipmentSelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(equipmentTypes) { equipment ->
            FilterChip(
                selected = equipment in selectedEquipment,
                onClick = { onEquipmentSelected(equipment) },
                label = { Text(equipment) }
            )
        }
    }
}

/**
 * Displays the main grid of exercises.
 */
@Composable
private fun ExerciseGrid(
    exercises: List<Exercise>,
    selectedExerciseIds: Set<Int>,
    onExerciseToggled: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(exercises, key = { it.id }) { exercise ->
            ExerciseGridCard(
                exercise = exercise,
                isSelected = exercise.id in selectedExerciseIds,
                onCardClick = { onExerciseToggled(exercise.id) }
            )
        }
    }
}

/**
 * A single card in the exercise grid.
 */
@Composable
private fun ExerciseGridCard(
    exercise: Exercise,
    isSelected: Boolean,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f) // Makes the card square
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            ),
        onClick = onCardClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // TODO: Replace with a GIF/Image later
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.3f)
                )
            }

            // Exercise Name overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(8.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = exercise.name,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Selection checkmark overlay
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

/**
 * The bottom bar that shows the number of selected items and the add button.
 */
@Composable
private fun AddExercisesBottomBar(
    selectedCount: Int,
    onAddClick: () -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onAddClick,
                enabled = selectedCount > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add $selectedCount Exercise${if (selectedCount > 1) "s" else ""}")
            }
        }
    }
}