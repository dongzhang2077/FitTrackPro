package com.domcheung.fittrackpro.presentation.plan_builder

import com.domcheung.fittrackpro.data.model.PlannedExercise

/**
 * Represents the UI state for the Plan Builder screen.
 */
data class PlanBuilderState(
    val isLoading: Boolean = false,
    val planName: String = "Untitled Plan",
    val description: String = "",
    val exercises: List<PlannedExercise> = emptyList(),
    val isEditingName: Boolean = false,
    val errorMessage: String? = null,
    val isPlanSaved: Boolean = false
)