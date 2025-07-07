package com.domcheung.fittrackpro.presentation.workout_session

data class WorkoutSessionState(
    // Loading and error states
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    // State flag specifically for the rest countdown UI
    val isCurrentlyResting: Boolean = false,

    // Session completion states
    val workoutCompleted: Boolean = false,
    val workoutAbandoned: Boolean = false,

    // Timers
    val elapsedTime: Long = 0L,
    val restTimeRemaining: Long = 0L,
    val totalRestTime: Long = 0L,

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
    val canCompleteWorkout: Boolean
        get() = elapsedTime > 0
}