package com.domcheung.fittrackpro.presentation.workout

import com.domcheung.fittrackpro.data.model.WorkoutPlan

/**
 * UI State for the Workout screen.
 * Holds all the state information required by the WorkoutScreen composable,
 * such as loading status, search queries, and one-time events.
 */
data class WorkoutUiState(
    val isLoading: Boolean = false,
    val isStartingWorkout: Boolean = false,
    val isCopyingPlan: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",

    // One-time events to be consumed by the UI
    val workoutStarted: Boolean = false,
    val planCopied: Boolean = false,
    val startedSessionId: String? = null,
    val copiedPlanId: String? = null,
    val planToDelete: WorkoutPlan? = null
) {
    /**
     * A computed property to easily check if any background operation is in progress.
     */
    val isAnyOperationInProgress: Boolean
        get() = isLoading || isStartingWorkout || isCopyingPlan
}