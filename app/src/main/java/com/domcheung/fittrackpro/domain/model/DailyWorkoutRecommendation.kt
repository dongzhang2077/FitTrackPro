package com.domcheung.fittrackpro.domain.model

import com.domcheung.fittrackpro.data.model.WorkoutPlan

data class DailyWorkoutRecommendation(
    val plan: WorkoutPlan?,
    val reason: String,
    val source: RecommendationSource
)

enum class RecommendationSource {
    AI,
    SMART_FALLBACK,
    NONE
}
