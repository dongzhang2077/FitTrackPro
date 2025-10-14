package com.domcheung.fittrackpro.presentation.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.local.UserPreferencesManager
import com.domcheung.fittrackpro.data.model.PersonalRecord
import com.domcheung.fittrackpro.data.repository.AuthRepository
import com.domcheung.fittrackpro.data.repository.WorkoutStatistics
import com.domcheung.fittrackpro.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getWorkoutStatisticsUseCase: GetWorkoutStatisticsUseCase,
    private val getPersonalRecordsUseCase: GetPersonalRecordsUseCase,
    private val getWeeklyWorkoutSummaryUseCase: GetWeeklyWorkoutSummaryUseCase,
    private val getMonthlyWorkoutSummaryUseCase: GetMonthlyWorkoutSummaryUseCase,
    private val getYearlyWorkoutSummaryUseCase: GetYearlyWorkoutSummaryUseCase,
    private val authRepository: AuthRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private val currentUserId: StateFlow<String?> = authRepository.isLoggedIn()
        .map { isLoggedIn ->
            if (isLoggedIn) authRepository.getCurrentUser()?.uid else null
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val weeklyWorkoutGoal = userPreferencesManager.weeklyWorkoutGoal
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 3
        )

    val workoutStatistics: StateFlow<WorkoutStatistics?> = currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                flow<WorkoutStatistics?> {
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

    val personalRecords: StateFlow<List<PersonalRecord>> = currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                getPersonalRecordsUseCase.getAllRecords(userId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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

    val filteredData: StateFlow<ProgressData> = combine(
        uiState.map { it.selectedTimeRange },
        workoutStatistics,
        personalRecords,
        recentPersonalRecords,
        currentUserId
    ) { timeRange, stats, allRecords, recentRecords, userId ->
        val weeklyActivity = if (userId != null) {
            getWeeklyWorkoutSummaryUseCase(userId)
        } else {
            emptyList()
        }
        val monthlyProgress = if (userId != null) {
            getMonthlyWorkoutSummaryUseCase(userId)
        } else {
            emptyList()
        }
        val yearlyProgress = if (userId != null) {
            getYearlyWorkoutSummaryUseCase(userId)
        } else {
            emptyList()
        }
        ProgressData(
            timeRange = timeRange,
            statistics = stats,
            personalRecords = filterRecordsByTimeRange(allRecords, timeRange),
            recentPersonalRecords = recentRecords,
            weeklyActivity = weeklyActivity,
            monthlyProgress = monthlyProgress,
            yearlyProgress = yearlyProgress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProgressData()
    )

    init {
        loadProgressData()
    }

    private fun loadProgressData() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load progress data"
                )
            }
        }
    }

    fun changeTimeRange(timeRange: ProgressTimeRange) {
        _uiState.value = _uiState.value.copy(selectedTimeRange = timeRange)
    }

    fun getPersonalRecordsByExercise(exerciseId: Int): Flow<List<PersonalRecord>> {
        val userId = currentUserId.value ?: return flowOf(emptyList())
        return getPersonalRecordsUseCase.getRecordsByExercise(userId, exerciseId)
    }

    fun getTopWeightRecords(limit: Int = 5): List<PersonalRecord> {
        return personalRecords.value
            .filter { it.recordType.name == "MAX_WEIGHT" }
            .sortedByDescending { it.weight }
            .take(limit)
    }

    fun getRecordsByCategory(): Map<String, List<PersonalRecord>> {
        return personalRecords.value.groupBy { it.exerciseName }
    }

    fun getAchievementStats(): AchievementStats {
        val allRecords = personalRecords.value
        val recentRecords = recentPersonalRecords.value
        val currentStreak = workoutStatistics.value?.currentStreak ?: 0

        return AchievementStats(
            totalPersonalRecords = allRecords.size,
            recentPersonalRecords = recentRecords.size,
            currentStreak = currentStreak,
            totalWorkouts = workoutStatistics.value?.totalWorkouts ?: 0,
            averageCompletion = workoutStatistics.value?.averageCompletionRate ?: 0f
        )
    }

    fun refreshProgress() {
        loadProgressData()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun filterRecordsByTimeRange(
        records: List<PersonalRecord>,
        timeRange: ProgressTimeRange
    ): List<PersonalRecord> {
        val cutoffTime = when (timeRange) {
            ProgressTimeRange.WEEK -> System.currentTimeMillis() - (7L * 24L * 60L * 60L * 1000L)
            ProgressTimeRange.MONTH -> System.currentTimeMillis() - (30L * 24L * 60L * 60L * 1000L)
            ProgressTimeRange.YEAR -> System.currentTimeMillis() - (365L * 24L * 60L * 60L * 1000L)
        }

        return records.filter { it.achievedAt >= cutoffTime }
    }
}

enum class ProgressTimeRange(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

data class ProgressUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTimeRange: ProgressTimeRange = ProgressTimeRange.WEEK
)

data class ProgressData(
    val timeRange: ProgressTimeRange = ProgressTimeRange.WEEK,
    val statistics: WorkoutStatistics? = null,
    val personalRecords: List<PersonalRecord> = emptyList(),
    val recentPersonalRecords: List<PersonalRecord> = emptyList(),
    val weeklyActivity: List<WeeklyActivityData> = emptyList(),
    val monthlyProgress: List<MonthlyProgressData> = emptyList(),
    val yearlyProgress: List<MonthlyProgressData> = emptyList()
)

data class WeeklyActivityData(
    val day: String,
    val dayOfMonth: Int,
    val isCompleted: Boolean,
    val isPast: Boolean,
    val isToday: Boolean
)

data class MonthlyProgressData(
    val month: String,
    val year: Int,
    val workoutCount: Int,
    val totalVolume: Float,
    val averageCompletion: Float
)

data class AchievementStats(
    val totalPersonalRecords: Int,
    val recentPersonalRecords: Int,
    val currentStreak: Int,
    val totalWorkouts: Int,
    val averageCompletion: Float
)