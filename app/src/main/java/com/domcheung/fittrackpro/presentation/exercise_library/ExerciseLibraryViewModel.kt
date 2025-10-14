package com.domcheung.fittrackpro.presentation.exercise_library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.domain.usecase.GetAllExercisesUseCase
import com.domcheung.fittrackpro.domain.usecase.SyncExercisesFromApiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * ViewModel for the Exercise Library screen.
 * Handles loading, filtering, searching, and selecting exercises.
 */
@HiltViewModel
class ExerciseLibraryViewModel @Inject constructor(
    private val getAllExercisesUseCase: GetAllExercisesUseCase,
    private val syncExercisesFromApiUseCase: SyncExercisesFromApiUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseLibraryState())
    val uiState: StateFlow<ExerciseLibraryState> = _uiState.asStateFlow()

    // Sync state for API data fetching
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    // This flow will hold the final, filtered list of exercises to be displayed on the UI.
    val filteredExercises: StateFlow<List<com.domcheung.fittrackpro.data.model.Exercise>> =
        combine(
            _uiState.map { it.allExercises }.distinctUntilChanged(),
            _uiState.map { it.searchQuery }.distinctUntilChanged(),
            _uiState.map { it.selectedMuscleGroup }.distinctUntilChanged(),
            _uiState.map { it.selectedEquipment }.distinctUntilChanged()
        ) { allExercises, query, muscle, equipment ->
            // This is the reactive filtering logic. It re-runs whenever any filter changes.
            allExercises.filter { exercise ->
                val nameMatches = query.isBlank() || exercise.name.contains(query, ignoreCase = true)
                val muscleGroupMatches = muscle == "All" || exercise.category.equals(muscle, ignoreCase = true)
                val equipmentMatches = equipment.isEmpty() || exercise.equipment.any { it in equipment }
                nameMatches && muscleGroupMatches && equipmentMatches
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadAllExercises()
        syncExercises()
    }

    /**
     * Loads all exercises from the repository and sets up the filter options.
     */
    private fun loadAllExercises() {
        viewModelScope.launch {
            getAllExercisesUseCase().collect { exercises ->
                // Extract unique categories and equipment for the filter chips.
                val muscleGroups = listOf("All") + exercises.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
                val equipmentTypes = exercises.flatMap { it.equipment }.filter { it.isNotBlank() }.distinct().sorted()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allExercises = exercises,
                        muscleGroupFilters = muscleGroups,
                        equipmentFilters = equipmentTypes,
                        // Set the default selected muscle group to the first one in the list if available.
                        selectedMuscleGroup = muscleGroups.firstOrNull() ?: "All"
                    )
                }
            }
        }
    }

    /**
     * Syncs exercises from the Wger API.
     * Call this when user pulls to refresh or taps a sync button.
     */
    private fun syncExercises() {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading

            val result = syncExercisesFromApiUseCase()

            _syncState.value = when {
                result.isSuccess -> SyncState.Success
                else -> SyncState.Error(result.exceptionOrNull()?.message ?: "Failed to sync exercises")
            }

            // Reset state after 3 seconds
            delay(3000)
            _syncState.value = SyncState.Idle
        }
    }

    /**
     * Called when the user types in the search bar.
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Called when the user selects a muscle group from the primary filter.
     */
    fun onMuscleGroupSelected(muscleGroup: String) {
        _uiState.update { it.copy(selectedMuscleGroup = muscleGroup) }
    }

    /**
     * Called when the user taps on an equipment filter chip.
     * It adds the equipment to the selection set if it's not present, or removes it if it is.
     */
    fun onEquipmentSelected(equipment: String) {
        _uiState.update {
            val currentSelection = it.selectedEquipment.toMutableSet()
            if (currentSelection.contains(equipment)) {
                currentSelection.remove(equipment)
            } else {
                currentSelection.add(equipment)
            }
            it.copy(selectedEquipment = currentSelection)
        }
    }

    /**
     * Called when the user taps the '+' or '✓' on an exercise card.
     * It adds the exercise ID to the selection set if it's not present, or removes it if it is.
     */
    fun onExerciseToggled(exerciseId: Int) {
        _uiState.update {
            val currentSelection = it.selectedExerciseIds.toMutableSet()
            if (currentSelection.contains(exerciseId)) {
                currentSelection.remove(exerciseId)
            } else {
                currentSelection.add(exerciseId)
            }
            it.copy(selectedExerciseIds = currentSelection)
        }
    }

    /**
     * Clears all current selections.
     */
    fun clearSelections() {
        _uiState.update { it.copy(selectedExerciseIds = emptySet()) }
    }
}

/**
 * Represents the sync state for exercise data from API
 */
sealed class SyncState {
    object Idle : SyncState()
    object Loading : SyncState()
    object Success : SyncState()
    data class Error(val message: String) : SyncState()
}