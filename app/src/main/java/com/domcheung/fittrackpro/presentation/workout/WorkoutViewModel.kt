package com.domcheung.fittrackpro.presentation.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.model.WorkoutPlan
import com.domcheung.fittrackpro.data.repository.AuthRepository
import com.domcheung.fittrackpro.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Workout tab screen
 * Manages workout plans display and user interactions
 */
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val getUserWorkoutPlansUseCase: GetUserWorkoutPlansUseCase,
    private val createWorkoutPlanUseCase: CreateWorkoutPlanUseCase,
    private val copyWorkoutPlanUseCase: CopyWorkoutPlanUseCase,
    private val startWorkoutSessionUseCase: StartWorkoutSessionUseCase,
    private val getActiveWorkoutSessionUseCase: GetActiveWorkoutSessionUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    // User workout plans
    val userWorkoutPlans: StateFlow<List<WorkoutPlan>> = authRepository.isLoggedIn()
        .flatMapLatest { isLoggedIn ->
            if (isLoggedIn) {
                // Get current user ID and fetch plans
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    getUserWorkoutPlansUseCase(currentUser.uid)
                } else {
                    flowOf(emptyList())
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Active workout session
    val activeWorkoutSession = authRepository.isLoggedIn()
        .flatMapLatest { isLoggedIn ->
            if (isLoggedIn) {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    getActiveWorkoutSessionUseCase.flow(currentUser.uid)
                } else {
                    flowOf(null)
                }
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        // Load initial data
        loadWorkoutPlans()
    }

    /**
     * Load user workout plans
     */
    private fun loadWorkoutPlans() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                // Data is automatically loaded through StateFlow
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load workout plans"
                )
            }
        }
    }

    /**
     * Start a workout session
     */
    fun startWorkout(planId: String) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "User not logged in"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isStartingWorkout = true)

        viewModelScope.launch {
            val result = startWorkoutSessionUseCase(planId, currentUser.uid)

            result.fold(
                onSuccess = { session ->
                    _uiState.value = _uiState.value.copy(
                        isStartingWorkout = false,
                        workoutStarted = true,
                        startedSessionId = session.id,
                        errorMessage = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isStartingWorkout = false,
                        errorMessage = exception.message ?: "Failed to start workout"
                    )
                }
            )
        }
    }

    /**
     * Copy workout plan
     */
    fun copyWorkoutPlan(planId: String, newName: String) {
        _uiState.value = _uiState.value.copy(isCopyingPlan = true)

        viewModelScope.launch {
            val result = copyWorkoutPlanUseCase(planId, newName)

            result.fold(
                onSuccess = { newPlanId ->
                    _uiState.value = _uiState.value.copy(
                        isCopyingPlan = false,
                        planCopied = true,
                        copiedPlanId = newPlanId,
                        errorMessage = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isCopyingPlan = false,
                        errorMessage = exception.message ?: "Failed to copy workout plan"
                    )
                }
            )
        }
    }

    /**
     * Navigate to create new plan screen
     */
    fun navigateToCreatePlan() {
        _uiState.value = _uiState.value.copy(
            showCreatePlanDialog = true
        )
    }

    /**
     * Dismiss create plan dialog
     */
    fun dismissCreatePlanDialog() {
        _uiState.value = _uiState.value.copy(
            showCreatePlanDialog = false
        )
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Clear one-time events
     */
    fun clearEvents() {
        _uiState.value = _uiState.value.copy(
            workoutStarted = false,
            planCopied = false,
            startedSessionId = null,
            copiedPlanId = null
        )
    }

    /**
     * Refresh workout plans
     */
    fun refreshWorkoutPlans() {
        loadWorkoutPlans()
    }

    /**
     * Search workout plans
     */
    fun searchWorkoutPlans(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query
        )
    }

    /**
     * Get filtered workout plans based on search query
     */
    fun getFilteredWorkoutPlans(): StateFlow<List<WorkoutPlan>> {
        return combine(
            userWorkoutPlans,
            uiState.map { it.searchQuery }
        ) { plans, query ->
            if (query.isBlank()) {
                plans
            } else {
                plans.filter { plan ->
                    plan.name.contains(query, ignoreCase = true) ||
                            plan.description.contains(query, ignoreCase = true) ||
                            plan.targetMuscleGroups.any { it.contains(query, ignoreCase = true) }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    /**
     * Get workout plan by ID
     */
    fun getWorkoutPlanById(planId: String): WorkoutPlan? {
        return userWorkoutPlans.value.find { it.id == planId }
    }

    /**
     * Check if user has active workout session
     */
    fun hasActiveWorkoutSession(): Boolean {
        return activeWorkoutSession.value != null
    }

    /**
     * Get active workout session ID
     */
    fun getActiveWorkoutSessionId(): String? {
        return activeWorkoutSession.value?.id
    }
}

/**
 * UI State for Workout screen
 */
data class WorkoutUiState(
    val isLoading: Boolean = false,
    val isStartingWorkout: Boolean = false,
    val isCopyingPlan: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val showCreatePlanDialog: Boolean = false,

    // One-time events
    val workoutStarted: Boolean = false,
    val planCopied: Boolean = false,
    val startedSessionId: String? = null,
    val copiedPlanId: String? = null
) {
    val isAnyOperationInProgress: Boolean
        get() = isLoading || isStartingWorkout || isCopyingPlan
}