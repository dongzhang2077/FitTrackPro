package com.domcheung.fittrackpro.domain.usecase

import com.domcheung.fittrackpro.data.repository.WorkoutRepository
import com.domcheung.fittrackpro.presentation.progress.WeeklyActivityData
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

class GetWeeklyWorkoutSummaryUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(userId: String): List<WeeklyActivityData> {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val weekData = mutableListOf<WeeklyActivityData>()

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val startDate = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endDate = calendar.timeInMillis

        val sessions = workoutRepository.getWorkoutSessionsByDateRange(userId, startDate, endDate).first()

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        repeat(7) {
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val dayName = getDayName(dayOfWeek)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val isCompleted = sessions.any {
                val sessionCalendar = Calendar.getInstance()
                sessionCalendar.timeInMillis = it.startTime
                sessionCalendar.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
            }

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
}