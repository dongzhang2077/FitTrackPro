package com.domcheung.fittrackpro.presentation.home

data class HomeUiState(
    val isLoading: Boolean = false,
    val isAnyOperationInProgress: Boolean = false,
    val errorMessage: String? = null,
    val workoutStarted: Boolean = false,
    val startedSessionId: String? = null,
    val workoutResumed: Boolean = false,
    val resumedSessionId: String? = null,
    val navigateToWorkoutTab: Boolean = false,
    val hasUnsyncedData: Boolean = false,
    val isSyncing: Boolean = false
)