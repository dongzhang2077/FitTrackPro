package com.domcheung.fittrackpro.domain.usecase

import com.domcheung.fittrackpro.data.model.WorkoutPlan
import com.domcheung.fittrackpro.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetTodaysRecommendedPlanUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(userId: String): WorkoutPlan? {
        // For now, just return the first plan
        // In the future, this could be a more complex algorithm
        return workoutRepository.getUserWorkoutPlans(userId).first().firstOrNull()
    }
}