package com.domcheung.fittrackpro.presentation.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.model.PersonalRecord
import com.domcheung.fittrackpro.data.model.WorkoutSession
import com.domcheung.fittrackpro.data.repository.AuthRepository
import com.domcheung.fittrackpro.data.repository.WorkoutStatistics
import com.domcheung.fittrackpro.data.repository.MonthlyWorkoutSummary
import com.domcheung.fittrackpro.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for Progress tab screen
 * Manages workout statistics, personal records, and progress analytics
 */
@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getWorkoutStatisticsUseCase: GetWorkoutStatisticsUseCase,
    private val getPersonalRecordsUseCase: GetPersonalRecordsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

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

    // Workout statistics
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

    // Personal records
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

    // Recent personal records (last 30 days)
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

    // Time range filtered data
    val filteredData: StateFlow<ProgressData> = combine(
        uiState.map { it.selectedTimeRange },
        workoutStatistics,
        personalRecords,
        recentPersonalRecords
    ) { timeRange, stats, allRecords, recentRecords ->
        ProgressData(
            timeRange = timeRange,
            statistics = stats,
            personalRecords = filterRecordsByTimeRange(allRecords, timeRange),
            recentPersonalRecords = recentRecords,
            weeklyActivity = generateWeeklyActivity(),
            monthlyProgress = generateMonthlyProgress(stats)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProgressData()
    )

    init {
        loadProgressData()
    }

    /**
     * Load all progress data
     */
    private fun loadProgressData() {
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
                    errorMessage = e.message ?: "Failed to load progress data"
                )
            }
        }
    }

    /**
     * Change time range filter
     */
    fun changeTimeRange(timeRange: ProgressTimeRange) {
        _uiState.value = _uiState.value.copy(selectedTimeRange = timeRange)
    }

    /**
     * Get personal records by exercise
     */
    fun getPersonalRecordsByExercise(exerciseId: Int): Flow<List<PersonalRecord>> {
        val userId = currentUserId.value ?: return flowOf(emptyList())
        return getPersonalRecordsUseCase.getRecordsByExercise(userId, exerciseId)
    }

    /**
     * Get top personal records by weight
     */
    fun getTopWeightRecords(limit: Int = 5): List<PersonalRecord> {
        return personalRecords.value
            .filter { it.recordType.name == "MAX_WEIGHT" }
            .sortedByDescending { it.weight }
            .take(limit)
    }

    /**
     * Get personal records by category
     */
    fun getRecordsByCategory(): Map<String, List<PersonalRecord>> {
        return personalRecords.value.groupBy { it.exerciseName }
    }

    /**
     * Get achievement statistics
     */
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

    /**
     * Get weekly activity data for chart
     */
    fun getWeeklyActivityData(): List<WeeklyActivityData> {
        return generateWeeklyActivity()
    }

    /**
     * Refresh progress data
     */
    fun refreshProgress() {
        loadProgressData()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Filter personal records by time range
     */
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

    /**
     * Generate weekly activity data
     */
    private fun generateWeeklyActivity(): List<WeeklyActivityData> {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val weekData = mutableListOf<WeeklyActivityData>()

        // Generate data for current week (Sunday to Saturday)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        repeat(7) { dayIndex ->
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val dayName = getDayName(dayOfWeek)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            // Simple logic: mark as completed if it's a past day (for demo)
            val isCompleted = dayOfWeek < today || (dayOfWeek == today && dayIndex % 2 == 0)

            weekData.add(
                WeeklyActivityData(
                    day = dayName,
                    dayOfMonth = dayOfMonth,
                    isCompleted = isCompleted,
                    isPast = dayOfWeek < today,
                    isToday = dayOfWeek == today
                )
            )

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return weekData
    }

    /**
     * Generate monthly progress data
     */
    private fun generateMonthlyProgress(statistics: WorkoutStatistics?): List<MonthlyProgressData> {
        val progressData = mutableListOf<MonthlyProgressData>()
        val calendar = Calendar.getInstance()

        // Generate data for last 6 months
        repeat(6) {
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)
            val monthName = getMonthName(month)

            // Simple demo data - in real app this would come from database
            val workouts = if (it == 0) statistics?.totalWorkouts?.let { total ->
                minOf(total, 12)
            } ?: 0 else (5..15).random()

            progressData.add(0,
                MonthlyProgressData(
                    month = monthName,
                    year = year,
                    workoutCount = workouts,
                    totalVolume = workouts * 1500f, // Estimated volume
                    averageCompletion = 75f + (0..20).random()
                )
            )

            calendar.add(Calendar.MONTH, -1)
        }

        return progressData
    }

    /**
     * Get day name from Calendar day of week
     */
    private fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "Sun"
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> "Unknown"
        }
    }

    /**
     * Get month name from month number
     */
    private fun getMonthName(month: Int): String {
        val months = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return months.getOrElse(month - 1) { "Unknown" }
    }
}

/**
 * Time range for filtering progress data (renamed to avoid conflicts)
 */
enum class ProgressTimeRange(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

/**
 * UI State for Progress screen
 */
data class ProgressUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTimeRange: ProgressTimeRange = ProgressTimeRange.WEEK
)

/**
 * Combined progress data
 */
data class ProgressData(
    val timeRange: ProgressTimeRange = ProgressTimeRange.WEEK,
    val statistics: WorkoutStatistics? = null,
    val personalRecords: List<PersonalRecord> = emptyList(),
    val recentPersonalRecords: List<PersonalRecord> = emptyList(),
    val weeklyActivity: List<WeeklyActivityData> = emptyList(),
    val monthlyProgress: List<MonthlyProgressData> = emptyList()
)

/**
 * Weekly activity data for visualization
 */
data class WeeklyActivityData(
    val day: String,
    val dayOfMonth: Int,
    val isCompleted: Boolean,
    val isPast: Boolean,
    val isToday: Boolean
)

/**
 * Monthly progress data for charts
 */
data class MonthlyProgressData(
    val month: String,
    val year: Int,
    val workoutCount: Int,
    val totalVolume: Float,
    val averageCompletion: Float
)

/**
 * Achievement statistics summary
 */
data class AchievementStats(
    val totalPersonalRecords: Int,
    val recentPersonalRecords: Int,
    val currentStreak: Int,
    val totalWorkouts: Int,
    val averageCompletion: Float
)