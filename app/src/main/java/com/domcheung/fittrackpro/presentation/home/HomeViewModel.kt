package com.domcheung.fittrackpro.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.repository.AuthRepository
import com.domcheung.fittrackpro.data.local.UserPreferencesManager
import com.domcheung.fittrackpro.domain.model.DailyWorkoutRecommendation
import com.domcheung.fittrackpro.domain.model.RecommendationSource
import com.domcheung.fittrackpro.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val getActiveWorkoutSessionUseCase: GetActiveWorkoutSessionUseCase,
    private val getWorkoutStatisticsUseCase: GetWorkoutStatisticsUseCase,
    private val getTodaysRecommendedPlanUseCase: GetTodaysRecommendedPlanUseCase,
    private val startWorkoutSessionUseCase: StartWorkoutSessionUseCase,
    private val resumeWorkoutSessionUseCase: ResumeWorkoutSessionUseCase,
    private val getWeeklyWorkoutSummaryUseCase: GetWeeklyWorkoutSummaryUseCase,
    private val getWeeklyStreakUseCase: GetWeeklyStreakUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val activeWorkoutSession = getActiveWorkoutSessionUseCase.flow(authRepository.getCurrentUser()?.uid ?: "")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val workoutStatistics = flow {
        val userId = authRepository.getCurrentUser()?.uid ?: ""
        val result = getWorkoutStatisticsUseCase(userId)
        emit(result.getOrNull())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // User name flow from preferences
    val userName = userPreferencesManager.userName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    // Display name that reacts to changes
    val displayName = userName.map { name ->
        val trimmedName = name.trim()
        if (trimmedName.isNotEmpty()) {
            trimmedName
        } else {
            val email = authRepository.getCurrentUser()?.email.orEmpty()
            email.substringBefore('@').ifBlank { "Friend" }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Friend"
    )

    // Weekly workout goal from preferences
    val weeklyWorkoutGoal = userPreferencesManager.weeklyWorkoutGoal
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 3
        )

    // Weekly activity data
    val weeklyActivity = flow {
        val userId = authRepository.getCurrentUser()?.uid ?: ""
        if (userId.isNotEmpty()) {
            val weeklyData = getWeeklyWorkoutSummaryUseCase(userId)
            emit(weeklyData)
        } else {
            emit(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactive weekly workout count
    val weeklyWorkoutCount = weeklyActivity.map { activity ->
        activity.count { it.isCompleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Reactive weekly progress percentage
    val weeklyProgress = weeklyWorkoutCount.combine(weeklyWorkoutGoal) { count, goal ->
        if (goal > 0) (count.toFloat() / goal.toFloat()) * 100f else 0f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val todaysRecommendation: StateFlow<DailyWorkoutRecommendation> = authRepository.isLoggedIn()
        .flatMapLatest { isLoggedIn ->
            if (!isLoggedIn) {
                flowOf(
                    DailyWorkoutRecommendation(
                        plan = null,
                        reason = "Sign in to receive a personalized recommendation.",
                        source = RecommendationSource.NONE
                    )
                )
            } else {
                flow {
                    val userId = authRepository.getCurrentUser()?.uid.orEmpty()
                    emit(getTodaysRecommendedPlanUseCase(userId))
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyWorkoutRecommendation(
                plan = null,
                reason = "Preparing your recommendation...",
                source = RecommendationSource.NONE
            )
        )

    fun getUserDisplayName(): String {
        return displayName.value
    }

    fun quickStartWorkout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnyOperationInProgress = true) }
            val userId = authRepository.getCurrentUser()?.uid.orEmpty()
            val recommendation = getTodaysRecommendedPlanUseCase(userId)
            val plan = recommendation.plan
            if (plan != null) {
                val result = startWorkoutSessionUseCase(plan.id, userId)
                result.fold(
                    onSuccess = { session ->
                        _uiState.update {
                            it.copy(
                                isAnyOperationInProgress = false,
                                workoutStarted = true,
                                startedSessionId = session.id
                            )
                        }
                    },
                    onFailure = {
                        _uiState.update {
                            it.copy(
                                isAnyOperationInProgress = false,
                                errorMessage = "Failed to start workout"
                            )
                        }
                    }
                )
            } else {
                _uiState.update {
                    it.copy(
                        isAnyOperationInProgress = false,
                        navigateToWorkoutTab = true
                    )
                }
            }
        }
    }

    fun resumeActiveWorkout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnyOperationInProgress = true) }
            val session = activeWorkoutSession.value
            if (session != null) {
                val result = resumeWorkoutSessionUseCase(session.id)
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isAnyOperationInProgress = false,
                                workoutResumed = true,
                                resumedSessionId = session.id
                            )
                        }
                    },
                    onFailure = {
                        _uiState.update {
                            it.copy(
                                isAnyOperationInProgress = false,
                                errorMessage = "Failed to resume workout"
                            )
                        }
                    }
                )
            } else {
                _uiState.update {
                    it.copy(
                        isAnyOperationInProgress = false,
                        errorMessage = "No active workout to resume"
                    )
                }
            }
        }
    }

    fun getWeeklyProgress(): Float {
        return weeklyProgress.value
    }

    fun getCurrentStreak(): Int {
        val userId = authRepository.getCurrentUser()?.uid ?: return 0
        val weeklyGoal = weeklyWorkoutGoal.value

        // Use coroutine to get the weekly streak
        return runBlocking {
            getWeeklyStreakUseCase(userId, weeklyGoal)
        }
    }

    fun getThisWeekWorkoutCount(): Int {
        return weeklyWorkoutCount.value
    }

    fun getWeeklyGoal(): Int {
        return weeklyWorkoutGoal.value
    }

    fun saveWeeklyGoal(goal: Int) {
        viewModelScope.launch {
            userPreferencesManager.saveWeeklyWorkoutGoal(goal)
        }
    }

    fun clearEvents() {
        _uiState.update {
            it.copy(
                workoutStarted = false,
                startedSessionId = null,
                workoutResumed = false,
                resumedSessionId = null,
                navigateToWorkoutTab = false
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
