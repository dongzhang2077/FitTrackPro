package com.domcheung.fittrackpro.presentation.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.model.WorkoutPlan
import com.domcheung.fittrackpro.data.repository.AuthRepository
import com.domcheung.fittrackpro.data.repository.WorkoutRepository
import com.domcheung.fittrackpro.domain.usecase.*
import com.domcheung.fittrackpro.presentation.workout.WorkoutUiState // 确保这里有导入
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Workout tab screen.
 * It manages the state and business logic for displaying, searching,
 * and interacting with user's workout plans.
 */
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val getUserWorkoutPlansUseCase: GetUserWorkoutPlansUseCase,
    private val copyWorkoutPlanUseCase: CopyWorkoutPlanUseCase,
    private val startWorkoutSessionUseCase: StartWorkoutSessionUseCase,
    private val getActiveWorkoutSessionUseCase: GetActiveWorkoutSessionUseCase,
    private val authRepository: AuthRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    // Internal mutable state for the UI
    private val _uiState = MutableStateFlow(WorkoutUiState())
    // Exposed immutable state for the UI to observe
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    // A flow that emits the current user's workout plans, driven by login state.
    val userWorkoutPlans: StateFlow<List<WorkoutPlan>> = authRepository.isLoggedIn()
        .flatMapLatest { isLoggedIn ->
            if (isLoggedIn) {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    // Seed initial plans if the user is new and has no plans
                    viewModelScope.launch {
                        workoutRepository.seedInitialPlansIfEmpty(currentUser.uid)
                    }
                    // Fetch the user's workout plans
                    getUserWorkoutPlansUseCase(currentUser.uid)
                } else {
                    flowOf(emptyList()) // User is logged out, emit empty list
                }
            } else {
                flowOf(emptyList()) // User is not logged in, emit empty list
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // A flow that observes the currently active workout session.
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
        // Initial load trigger, though the flows above are self-starting.
        loadWorkoutPlans()
    }

    /**
     * Sets the initial loading state for the screen.
     */
    private fun loadWorkoutPlans() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        // The actual data loading is handled reactively by the StateFlows.
        // We can simply turn off the loading indicator after a short delay or when data arrives.
        viewModelScope.launch {
            // Wait for the first set of plans to be emitted
            userWorkoutPlans.first { it.isNotEmpty() || !authRepository.isLoggedIn().first() }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    /**
     * Starts a new workout session based on a selected plan.
     * @param planId The ID of the WorkoutPlan to start.
     */
    fun startWorkout(planId: String) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "User not logged in")
            return
        }
        _uiState.value = _uiState.value.copy(isStartingWorkout = true)
        viewModelScope.launch {
            val result = startWorkoutSessionUseCase(planId, currentUser.uid)
            result.fold(
                onSuccess = { session ->
                    _uiState.value = _uiState.value.copy(
                        isStartingWorkout = false,
                        workoutStarted = true, // This is a one-time event
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
     * Creates a copy of an existing workout plan.
     * @param planId The ID of the plan to copy.
     * @param newName The name for the new copied plan.
     */
    fun copyWorkoutPlan(planId: String, newName: String) {
        _uiState.value = _uiState.value.copy(isCopyingPlan = true)
        viewModelScope.launch {
            val result = copyWorkoutPlanUseCase(planId, newName)
            result.fold(
                onSuccess = { newPlanId ->
                    _uiState.value = _uiState.value.copy(
                        isCopyingPlan = false,
                        planCopied = true, // One-time event
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
     * Updates the search query in the UI state.
     * @param query The new search term.
     */
    fun searchWorkoutPlans(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    /**
     * Returns a flow of workout plans filtered by the current search query.
     */
    fun getFilteredWorkoutPlans(): StateFlow<List<WorkoutPlan>> {
        return combine(
            userWorkoutPlans,
            _uiState.map { it.searchQuery }.distinctUntilChanged()
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
            initialValue = userWorkoutPlans.value
        )
    }

    /**
     * Clears any displayed error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Resets one-time event flags in the UI state after they have been handled.
     */
    fun clearEvents() {
        _uiState.value = _uiState.value.copy(
            workoutStarted = false,
            planCopied = false,
            startedSessionId = null,
            copiedPlanId = null
        )
    }
}