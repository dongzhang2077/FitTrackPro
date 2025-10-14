package com.domcheung.fittrackpro.presentation.plan_builder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.domcheung.fittrackpro.data.model.PlannedExercise
import com.domcheung.fittrackpro.data.model.PlannedSet
import com.domcheung.fittrackpro.presentation.exercise_library.ExerciseLibraryScreen
import com.domcheung.fittrackpro.ui.theme.HandDrawnShapes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanBuilderScreen(
    viewModel: PlanBuilderViewModel = hiltViewModel(),
    navController: androidx.navigation.NavHostController,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    var isSheetOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = sheetState
        ) {
            ExerciseLibraryScreen(
                onClose = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            isSheetOpen = false
                        }
                    }
                },
                onAddExercises = { selectedIds ->
                    viewModel.addExercisesByIds(selectedIds)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            isSheetOpen = false
                        }
                    }
                },
                onExerciseClick = { exerciseId ->
                    navController.navigate(com.domcheung.fittrackpro.navigation.Routes.exerciseDetail(exerciseId))
                }
            )
        }
    }

    LaunchedEffect(uiState.isPlanSaved) {
        if (uiState.isPlanSaved) {
            onNavigateBack()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create Plan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.savePlan() }) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "Save Plan")
                    }
                }
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { isSheetOpen = true },
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
                PlanExerciseList(
                    exercises = uiState.exercises,
                    viewModel = viewModel,
                    onExerciseInfoClick = { exerciseId ->
                        navController.navigate(com.domcheung.fittrackpro.navigation.Routes.exerciseDetail(exerciseId))
                    },
                    expandedExerciseId = uiState.expandedExerciseId
                )
            }
        }
    }
}

@Composable
private fun PlanExerciseList(
    exercises: List<PlannedExercise>,
    viewModel: PlanBuilderViewModel,
    onExerciseInfoClick: (Int) -> Unit = {},
    expandedExerciseId: Int?
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        itemsIndexed(exercises, key = { _, item -> item.exerciseId }) { index, exercise ->
            SwipeablePlanExerciseItem(
                exercise = exercise,
                isExpanded = expandedExerciseId == exercise.exerciseId,
                onExerciseClicked = { viewModel.onExerciseClicked(exercise.exerciseId) },
                onInfoClick = { onExerciseInfoClick(exercise.exerciseId) },
                onAddSet = { viewModel.addSetToExercise(index) },
                onRemoveSet = { viewModel.removeSetFromExercise(index) },
                onSetChanged = { setIndex, updatedSet -> viewModel.updateSet(index, setIndex, updatedSet) },
                onDelete = { viewModel.removeExerciseById(exercise.exerciseId) }
            )
        }
    }
}

@Composable
private fun SwipeablePlanExerciseItem(
    exercise: PlannedExercise,
    isExpanded: Boolean,
    onExerciseClicked: () -> Unit,
    onInfoClick: () -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: () -> Unit,
    onSetChanged: (Int, PlannedSet) -> Unit,
    onDelete: () -> Unit
) {
    PlanExerciseItem(
        modifier = Modifier.fillMaxWidth(),
        exercise = exercise,
        onInfoClick = onInfoClick,
        isExpanded = isExpanded,
        onExerciseClicked = onExerciseClicked,
        onAddSet = onAddSet,
        onRemoveSet = onRemoveSet,
        onSetChanged = onSetChanged
    )
}

@Composable
private fun PlanExerciseItem(
    modifier: Modifier = Modifier,
    exercise: PlannedExercise,
    onInfoClick: () -> Unit,
    isExpanded: Boolean,
    onExerciseClicked: () -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: () -> Unit,
    onSetChanged: (Int, PlannedSet) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.medium)
            .clickable { onExerciseClicked() }
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                exercise.imageUrl?.let { imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUrl),
                        contentDescription = "Exercise Image",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = exercise.exerciseName, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Sets: ${exercise.sets.size}", style = MaterialTheme.typography.bodyMedium)
                }

                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                    )
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    exercise.sets.forEachIndexed { index, set ->
                        SetInputRow(
                            set = set,
                            onSetChanged = { updatedSet ->
                                onSetChanged(index, updatedSet)
                            }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onRemoveSet) {
                            Text("Remove Set")
                        }
                        TextButton(onClick = onAddSet) {
                            Text("Add Set")
                        }
                    }
                }
            }
        }
    }
}

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
            Text(
                text = name.ifBlank { "Untitled Plan" },
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            IconButton(onClick = { onToggleEdit(true) }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit name")
            }
        }
    }
}

@Composable
private fun EmptyPlanContent() {
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
