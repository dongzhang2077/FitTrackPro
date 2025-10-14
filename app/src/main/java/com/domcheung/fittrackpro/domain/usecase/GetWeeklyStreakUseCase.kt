package com.domcheung.fittrackpro.domain.usecase

import com.domcheung.fittrackpro.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for calculating the current weekly streak
 * Counts consecutive weeks where the user met their weekly workout goal
 */
@Singleton
class GetWeeklyStreakUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(userId: String, weeklyGoal: Int): Int {
        if (weeklyGoal <= 0) return 0

        val calendar = Calendar.getInstance()
        var streak = 0
        var currentWeekStart = getWeekStart(calendar.timeInMillis)

        // Check up to 52 weeks back (1 year)
        for (weekOffset in 0..51) {
            val weekStart = currentWeekStart - (weekOffset * 7L * 24L * 60L * 60L * 1000L)
            val weekEnd = weekStart + (6L * 24L * 60L * 60L * 1000L) + (23L * 60L * 60L * 1000L) + (59L * 60L * 1000L) + (59L * 1000L) + 999L

            val sessionsInWeek = repository.getWorkoutSessionsByDateRange(userId, weekStart, weekEnd)
                .first()
                .filter { it.status.name == "COMPLETED" }

            val workoutsCompleted = sessionsInWeek.size

            if (workoutsCompleted >= weeklyGoal) {
                streak++
            } else {
                // Streak breaks if goal not met
                break
            }
        }

        return streak
    }

    /**
     * Get the start of the week (Monday) for a given timestamp
     * Uses ISO week definition (Monday-Sunday)
     */
    private fun getWeekStart(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}