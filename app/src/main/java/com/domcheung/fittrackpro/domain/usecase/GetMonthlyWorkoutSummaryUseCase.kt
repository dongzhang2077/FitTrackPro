package com.domcheung.fittrackpro.domain.usecase

import com.domcheung.fittrackpro.data.repository.WorkoutRepository
import com.domcheung.fittrackpro.presentation.progress.MonthlyProgressData
import java.util.*
import javax.inject.Inject

class GetMonthlyWorkoutSummaryUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(userId: String): List<MonthlyProgressData> {
        val calendar = Calendar.getInstance()
        val monthData = mutableListOf<MonthlyProgressData>()

        repeat(6) {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val monthName = getMonthName(month)
            val summary = workoutRepository.getMonthlyWorkoutSummary(userId, year, month).getOrNull()

            monthData.add(
                0,
                MonthlyProgressData(
                    month = monthName,
                    year = year,
                    workoutCount = summary?.totalWorkouts ?: 0,
                    totalVolume = summary?.totalVolume ?: 0f,
                    averageCompletion = summary?.averageCompletion ?: 0f
                )
            )

            calendar.add(Calendar.MONTH, -1)
        }
        return monthData
    }

    private fun getMonthName(month: Int): String {
        val months = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return months.getOrElse(month - 1) { "Unknown" }
    }
}