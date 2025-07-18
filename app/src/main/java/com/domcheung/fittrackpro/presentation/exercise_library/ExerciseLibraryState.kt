package com.domcheung.fittrackpro.presentation.exercise_library

import com.domcheung.fittrackpro.data.model.Exercise

/**
 * Represents the UI state for the Exercise Library screen.
 * It holds all data needed for displaying and filtering exercises.
 */
data class ExerciseLibraryState(
    // Indicates if the initial list of exercises is being loaded.
    val isLoading: Boolean = true,

    // The complete, unfiltered list of exercises from the database.
    val allExercises: List<Exercise> = emptyList(),

    // The unique list of muscle group categories available for filtering.
    val muscleGroupFilters: List<String> = emptyList(),

    // The unique list of equipment types available for filtering.
    val equipmentFilters: List<String> = emptyList(),

    // User's current filter selections.
    val searchQuery: String = "",
    val selectedMuscleGroup: String = "All",
    val selectedEquipment: Set<String> = emptySet(),

    // The set of exercise IDs that the user has selected.
    val selectedExerciseIds: Set<Int> = emptySet(),

    // Any error messages to be displayed.
    val errorMessage: String? = null
)