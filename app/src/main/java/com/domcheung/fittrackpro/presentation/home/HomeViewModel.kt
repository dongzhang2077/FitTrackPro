package com.domcheung.fittrackpro.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.model.PersonalRecord
import com.domcheung.fittrackpro.data.model.WorkoutPlan
import com.domcheung.fittrackpro.data.model.WorkoutSession
import com.domcheung.fittrackpro.data.repository.AuthRepository
import com.domcheung.fittrackpro.data.repository.WorkoutStatistics
import com.domcheung.fittrackpro.domain.usecase.GetActiveWorkoutSessionUseCase
import com.domcheung.fittrackpro.domain.usecase.GetPersonalRecordsUseCase
import com.domcheung.fittrackpro.domain.usecase.GetUserWorkoutPlansUseCase
import com.domcheung.fittrackpro.domain.usecase.GetWorkoutStatisticsUseCase
import com.domcheung.fittrackpro.domain.usecase.StartWorkoutSessionUseCase
import com.domcheung.fittrackpro.domain.usecase.SyncDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for Home tab screen
 * Manages dashboard data, workout overview, and quick actions
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getActiveWorkoutSessionUseCase: GetActiveWorkoutSessionUseCase,
    private val getUserWorkoutPlansUseCase: GetUserWorkoutPlansUseCase,
    private val getWorkoutStatisticsUseCase: GetWorkoutStatisticsUseCase,
    private val getPersonalRecordsUseCase: GetPersonalRecordsUseCase,
    private val startWorkoutSessionUseCase: StartWorkoutSessionUseCase,
    private val syncDataUseCase: SyncDataUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Current user ID
    private val currentUserId: StateFlow<String?> = authRepository.isLoggedIn()
        .map { isLoggedIn ->
            if (isLoggedIn) authRepository.getCurrentUser()?.uid else null
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Active workout session
    val activeWorkoutSession: StateFlow<WorkoutSession?> = currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                getActiveWorkoutSessionUseCase.flow(userId)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // User workout plans for today's recommendation
    val userWorkoutPlans: StateFlow<List<WorkoutPlan>> = currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                getUserWorkoutPlansUseCase(userId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Recent personal records
    val recentPersonalRecords: StateFlow<List<PersonalRecord>> = currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                getPersonalRecordsUseCase.getRecentRecords(userId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Workout statistics
    val workoutStatistics: StateFlow<WorkoutStatistics?> = currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                flow<WorkoutStatistics?> {
                    emit(null) // Initial loading state
                    val result = getWorkoutStatisticsUseCase(userId)
                    emit(result.getOrNull())
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
        loadDashboardData()
        checkForDataSync()
    }

    /**
     * Load all dashboard data
     */
    private fun loadDashboardData() {
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
                    errorMessage = e.message ?: "Failed to load dashboard data"
                )
            }
        }
    }

    /**
     * Check if data needs to be synced
     */
    private fun checkForDataSync() {
        viewModelScope.launch {
            try {
                val needsSync = syncDataUseCase.hasUnsyncedData()
                _uiState.value = _uiState.value.copy(hasUnsyncedData = needsSync)
            } catch (e: Exception) {
                // Ignore sync check errors - not critical for home screen
            }
        }
    }

    /**
     * Get today's recommended workout plan
     */
    fun getTodaysRecommendedPlan(): WorkoutPlan? {
        val plans = userWorkoutPlans.value
        if (plans.isEmpty()) return null

        // Simple recommendation logic: rotate through plans based on day of week
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val planIndex = (dayOfWeek - 1) % plans.size
        return plans.getOrNull(planIndex)
    }

    /**
     * Quick start workout with recommended plan
     */
    fun quickStartWorkout() {
        val userId = currentUserId.value
        if (userId == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "User not logged in"
            )
            return
        }

        val recommendedPlan = getTodaysRecommendedPlan()
        if (recommendedPlan == null) {
            // If no plan is available, set the state to trigger navigation to the Workout tab.
            _uiState.update { it.copy(navigateToWorkoutTab = true) }
            return
        }

        startWorkout(recommendedPlan.id)
    }

    /**
     * Start specific workout
     */
    fun startWorkout(planId: String) {
        val userId = currentUserId.value
        if (userId == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "User not logged in"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isStartingWorkout = true)

        viewModelScope.launch {
            val result = startWorkoutSessionUseCase(planId, userId)

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
     * Resume active workout
     */
    fun resumeActiveWorkout() {
        val activeSession = activeWorkoutSession.value
        if (activeSession != null) {
            _uiState.value = _uiState.value.copy(
                workoutResumed = true,
                resumedSessionId = activeSession.id
            )
        }
    }

    /**
     * Sync data to Firebase
     */
    fun syncData() {
        val userId = currentUserId.value
        if (userId == null) return

        _uiState.value = _uiState.value.copy(isSyncing = true)

        viewModelScope.launch {
            val result = syncDataUseCase(userId)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        hasUnsyncedData = false,
                        syncCompleted = true,
                        errorMessage = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        errorMessage = exception.message ?: "Sync failed"
                    )
                }
            )
        }
    }

    /**
     * Refresh all dashboard data
     */
    fun refreshDashboard() {
        loadDashboardData()
        checkForDataSync()
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
            workoutResumed = false,
            syncCompleted = false,
            startedSessionId = null,
            resumedSessionId = null,
            navigateToWorkoutTab = false
        )
    }

    /**
     * Get current workout progress
     */
    fun getCurrentWorkoutProgress(): Float {
        return activeWorkoutSession.value?.completionPercentage ?: 0f
    }

    /**
     * Check if user has active workout
     */
    fun hasActiveWorkout(): Boolean {
        return activeWorkoutSession.value != null
    }

    /**
     * Get current workout plan name
     */
    fun getCurrentWorkoutPlanName(): String? {
        return activeWorkoutSession.value?.planName
    }

    /**
     * Get workout streak
     */
    fun getCurrentStreak(): Int {
        return workoutStatistics.value?.currentStreak ?: 0
    }

    /**
     * Get this week's workout count
     */
    fun getThisWeekWorkoutCount(): Int {
        // This would be calculated from statistics
        // For now, return a simplified version
        return workoutStatistics.value?.totalWorkouts?.let { total ->
            minOf(total, 7) // Cap at 7 for weekly display
        } ?: 0
    }

    /**
     * Get weekly goal (hardcoded for now)
     */
    fun getWeeklyGoal(): Int {
        return 3 // Default weekly goal of 3 workouts
    }

    /**
     * Calculate weekly progress percentage
     */
    fun getWeeklyProgress(): Float {
        val completed = getThisWeekWorkoutCount()
        val goal = getWeeklyGoal()
        return if (goal > 0) (completed.toFloat() / goal.toFloat()) * 100f else 0f
    }

    /**
     * Get recent achievements (PRs in last 7 days)
     */
    fun getRecentAchievements(): List<PersonalRecord> {
        val sevenDaysAgo = System.currentTimeMillis() - (7L * 24L * 60L * 60L * 1000L)
        return recentPersonalRecords.value.filter { it.achievedAt >= sevenDaysAgo }
    }

    /**
     * Get user's display name
     */
    fun getUserDisplayName(): String {
        return authRepository.getCurrentUser()?.displayName?.takeIf { it?.isNotBlank() == true }
            ?: authRepository.getCurrentUser()?.email?.substringBefore('@')
            ?: "User"
    }
}

/**
 * UI State for Home screen
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val isStartingWorkout: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
    val hasUnsyncedData: Boolean = false,

    // One-time events
    val workoutStarted: Boolean = false,
    val workoutResumed: Boolean = false,
    val syncCompleted: Boolean = false,
    val startedSessionId: String? = null,
    val resumedSessionId: String? = null,
    val navigateToWorkoutTab: Boolean = false
) {
    val isAnyOperationInProgress: Boolean
        get() = isLoading || isStartingWorkout || isSyncing
}