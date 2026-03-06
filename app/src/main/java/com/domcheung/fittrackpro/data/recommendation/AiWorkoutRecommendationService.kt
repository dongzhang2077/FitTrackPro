package com.domcheung.fittrackpro.data.recommendation

import com.domcheung.fittrackpro.data.model.WorkoutPlan

interface AiWorkoutRecommendationService {
    suspend fun recommendPlan(context: AiRecommendationContext): AiPlanRecommendation?
}

data class AiRecommendationContext(
    val userId: String,
    val dayOfWeek: Int,
    val plans: List<WorkoutPlan>,
    val recentCompletedPlanIds: List<String>,
    val primaryGoal: String = "GENERAL_FITNESS",
    val preferredWorkoutFrequency: Int = 3,
    val experienceLevel: String = "BEGINNER"
)

data class AiPlanRecommendation(
    val planId: String,
    val reason: String
)
