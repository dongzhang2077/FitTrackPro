package com.domcheung.fittrackpro.presentation.workout_session

import com.domcheung.fittrackpro.data.model.WorkoutSession

/**
 * Represents the state of the workout session screen.
 * This data class holds all the information the UI needs to render itself.
 */
data class WorkoutSessionState(
    // Loading and error states
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    // Session status
    val isResting: Boolean = false,
    val workoutCompleted: Boolean = false,
    val workoutAbandoned: Boolean = false,

    // Timers
    val elapsedTime: Long = 0L, // in milliseconds
    val restTimeRemaining: Long = 0L, // in milliseconds
    val totalRestTime: Long = 0L, // in milliseconds

    // Current progress indices
    val currentExerciseIndex: Int = 0,
    val currentSetIndex: Int = 0,

    // User input for the current set
    val currentWeight: Float = 0f,
    val currentReps: Int = 0,

    // Dialog visibility flags
    val showAbandonDialog: Boolean = false,
    val showCompleteDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showReplaceExerciseDialog: Boolean = false
) {
    /**
     * Helper property to determine if the "Complete Workout" button should be enabled.
     * For example, enable only if at least one set has been completed.
     */
    val canCompleteWorkout: Boolean
        get() = elapsedTime > 0 // Simple logic: can complete if workout has started
}

/**
 * Defines all possible events (user actions) that can be triggered from the UI.
 * This sealed interface is used for communication from the View to the ViewModel.
 */
sealed interface WorkoutSessionEvent {
    data class LoadSession(val sessionId: String) : WorkoutSessionEvent
    data object PauseWorkout : WorkoutSessionEvent
    data object ResumeWorkout : WorkoutSessionEvent
    data object AbandonWorkout : WorkoutSessionEvent
    data object CompleteWorkout : WorkoutSessionEvent
    data object CompleteCurrentSet : WorkoutSessionEvent
    data object SkipCurrentSet : WorkoutSessionEvent
    data object SkipRest : WorkoutSessionEvent
    data class AdjustRestTime(val adjustment: Int) : WorkoutSessionEvent
    data class UpdateWeight(val weight: Float) : WorkoutSessionEvent
    data class UpdateReps(val reps: Int) : WorkoutSessionEvent
    data object AddSet : WorkoutSessionEvent
    data object RemoveSet : WorkoutSessionEvent

    // Dialog events
    data object ShowAbandonDialog : WorkoutSessionEvent
    data object HideAbandonDialog : WorkoutSessionEvent
    data object ShowCompleteDialog : WorkoutSessionEvent
    data object HideCompleteDialog : WorkoutSessionEvent
    data object ShowSettingsDialog : WorkoutSessionEvent
    data object HideSettingsDialog : WorkoutSessionEvent
    data object ShowReplaceExerciseDialog : WorkoutSessionEvent
    data object HideReplaceExerciseDialog : WorkoutSessionEvent

    data object ClearError : WorkoutSessionEvent
}