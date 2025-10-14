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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.domcheung.fittrackpro.data.model.Exercise
import com.domcheung.fittrackpro.ui.theme.HandDrawnShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    viewModel: ExerciseLibraryViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onAddExercises: (Set<Int>) -> Unit,
    onExerciseClick: (Int) -> Unit = {}
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
            AddExercisesBottomBar(
                selectedCount = uiState.selectedExerciseIds.size,
                onAddClick = {
                    onAddExercises(uiState.selectedExerciseIds)
                    onClose()
                }
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MuscleGroupFilterList(
                muscleGroups = uiState.muscleGroupFilters,
                selectedMuscleGroup = uiState.selectedMuscleGroup,
                onMuscleGroupSelected = viewModel::onMuscleGroupSelected
            )

            Column(modifier = Modifier.fillMaxSize()) {
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

                EquipmentFilterChips(
                    equipmentTypes = uiState.equipmentFilters,
                    selectedEquipment = uiState.selectedEquipment,
                    onEquipmentSelected = viewModel::onEquipmentSelected
                )

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    ExerciseGrid(
                        exercises = filteredExercises,
                        selectedExerciseIds = uiState.selectedExerciseIds,
                        onExerciseToggled = viewModel::onExerciseToggled,
                        onExerciseInfoClicked = { exercise ->
                            onExerciseClick(exercise.id)
                        }
                    )
                }
            }
        }
    }
}

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

@Composable
private fun ExerciseGrid(
    exercises: List<Exercise>,
    selectedExerciseIds: Set<Int>,
    onExerciseToggled: (Int) -> Unit,
    onExerciseInfoClicked: (Exercise) -> Unit
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
                onCardClick = { onExerciseToggled(exercise.id) },
                onInfoClick = { onExerciseInfoClicked(exercise) }
            )
        }
    }
}

@Composable
private fun ExerciseGridCard(
    exercise: Exercise,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = HandDrawnShapes.medium
            ),
        onClick = onCardClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (exercise.imageUrl != null) {
                AsyncImage(
                    model = exercise.imageUrl,
                    contentDescription = exercise.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            if (exercise.imageUrl == null) {
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
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(8.dp)
            ) {
                Text(
                    text = exercise.name,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
            }

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

            IconButton(
                onClick = onInfoClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = HandDrawnShapes.small
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "View Details",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

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