package com.domcheung.fittrackpro.presentation.plan_builder

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for the Plan Builder screen.
 * Manages the state and logic for creating and editing a workout plan.
 */
@HiltViewModel
class PlanBuilderViewModel @Inject constructor(
    // We will inject UseCases here in later steps
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanBuilderState())
    val uiState: StateFlow<PlanBuilderState> = _uiState

    // Functions to handle UI events will be added here later.

    /**
     * Called when the plan name is changed by the user in the text field.
     */
    fun onPlanNameChanged(newName: String) {
        _uiState.update { it.copy(planName = newName) }
    }

    /**
     * Toggles the UI between displaying the plan name and editing it.
     */
    fun onToggleEditName(isEditing: Boolean) {
        _uiState.update { it.copy(isEditingName = isEditing) }
    }

    // Add these new functions inside your PlanBuilderViewModel class

    /**
     * Adds a new exercise to the current plan.
     * In a real scenario, this would be called from the Exercise Library screen.
     * For now, we'll use it for testing.
     * @param exercise The exercise to add.
     */
    fun addExercise(exercise: com.domcheung.fittrackpro.data.model.Exercise) {
        val newPlannedExercise = com.domcheung.fittrackpro.data.model.PlannedExercise(
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            orderIndex = _uiState.value.exercises.size,
            // Add one default set when a new exercise is added.
            sets = listOf(com.domcheung.fittrackpro.data.model.PlannedSet(1, 20f, 10))
        )
        _uiState.update {
            it.copy(exercises = it.exercises + newPlannedExercise)
        }
    }

    /**
     * Removes an exercise from the plan based on its index in the list.
     * @param index The index of the exercise to remove.
     */
    fun removeExercise(index: Int) {
        _uiState.update {
            val updatedExercises = it.exercises.toMutableList().apply {
                removeAt(index)
            }
            it.copy(exercises = updatedExercises)
        }
    }

    /**
     * Adds a new set to a specific exercise in the plan.
     * It copies the details from the last existing set.
     * @param exerciseIndex The index of the exercise to modify.
     */
    fun addSetToExercise(exerciseIndex: Int) {
        _uiState.update {
            val exercises = it.exercises.toMutableList()
            val exercise = exercises.getOrNull(exerciseIndex) ?: return@update it

            val sets = exercise.sets.toMutableList()
            val lastSet = sets.lastOrNull() ?: com.domcheung.fittrackpro.data.model.PlannedSet(0, 20f, 10)

            // Create a new set by copying the last one.
            sets.add(lastSet.copy(setNumber = sets.size + 1))

            exercises[exerciseIndex] = exercise.copy(sets = sets)
            it.copy(exercises = exercises)
        }
    }

    /**
     * Removes the last set from a specific exercise.
     * @param exerciseIndex The index of the exercise to modify.
     */
    fun removeSetFromExercise(exerciseIndex: Int) {
        _uiState.update {
            val exercises = it.exercises.toMutableList()
            val exercise = exercises.getOrNull(exerciseIndex) ?: return@update it

            // Only remove if there is more than one set.
            if (exercise.sets.size > 1) {
                val sets = exercise.sets.toMutableList()
                sets.removeAt(sets.lastIndex)
                exercises[exerciseIndex] = exercise.copy(sets = sets)
                it.copy(exercises = exercises)
            } else {
                it // Return original state if only one set is left.
            }
        }
    }

    /**
     * Updates a specific set's details for a given exercise.
     * @param exerciseIndex The index of the exercise.
     * @param setIndex The index of the set within the exercise.
     * @param updatedSet The new data for the set.
     */
    fun updateSet(exerciseIndex: Int, setIndex: Int, updatedSet: com.domcheung.fittrackpro.data.model.PlannedSet) {
        _uiState.update {
            val exercises = it.exercises.toMutableList()
            val exercise = exercises.getOrNull(exerciseIndex) ?: return@update it

            val sets = exercise.sets.toMutableList()
            if (sets.getOrNull(setIndex) != null) {
                sets[setIndex] = updatedSet
                exercises[exerciseIndex] = exercise.copy(sets = sets)
                it.copy(exercises = exercises)
            } else {
                it
            }
        }
    }
}

