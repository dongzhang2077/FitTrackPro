package com.domcheung.fittrackpro.domain.usecase

import com.domcheung.fittrackpro.data.local.UserPreferencesManager
import com.domcheung.fittrackpro.data.model.WorkoutPlan
import com.domcheung.fittrackpro.data.model.WorkoutSession
import com.domcheung.fittrackpro.data.recommendation.AiRecommendationContext
import com.domcheung.fittrackpro.data.recommendation.AiWorkoutRecommendationService
import com.domcheung.fittrackpro.data.repository.WorkoutRepository
import com.domcheung.fittrackpro.domain.model.DailyWorkoutRecommendation
import com.domcheung.fittrackpro.domain.model.RecommendationSource
import kotlinx.coroutines.flow.first
import kotlin.math.abs
import java.time.LocalDate
import javax.inject.Inject

class GetTodaysRecommendedPlanUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val aiWorkoutRecommendationService: AiWorkoutRecommendationService,
    private val userPreferencesManager: UserPreferencesManager
) {
    private data class PlanScore(
        val plan: WorkoutPlan,
        val score: Int,
        val rotatedFromRecent: Boolean,
        val matchedGoal: Boolean,
        val durationAligned: Boolean
    )

    private data class FallbackSelection(
        val plan: WorkoutPlan,
        val reason: String
    )

    suspend operator fun invoke(userId: String): DailyWorkoutRecommendation {
        if (userId.isBlank()) {
            return DailyWorkoutRecommendation(
                plan = null,
                reason = "Sign in to receive a personalized recommendation.",
                source = RecommendationSource.NONE
            )
        }

        val plans = workoutRepository.getUserWorkoutPlans(userId).first()
        if (plans.isEmpty()) {
            return DailyWorkoutRecommendation(
                plan = null,
                reason = "Create your first workout plan to unlock recommendations.",
                source = RecommendationSource.NONE
            )
        }

        val completedSessions = workoutRepository
            .getCompletedWorkoutSessions(userId)
            .first()
            .sortedByDescending { session -> session.endTime ?: session.startTime }

        val dayOfWeek = LocalDate.now().dayOfWeek.value
        val primaryGoal = userPreferencesManager.fitnessPrimaryGoal.first()
        val preferredWorkoutFrequency = userPreferencesManager.fitnessWorkoutFrequency.first()
        val experienceLevel = userPreferencesManager.fitnessExperienceLevel.first()

        val aiRecommendation = runCatching {
            aiWorkoutRecommendationService.recommendPlan(
                AiRecommendationContext(
                    userId = userId,
                    dayOfWeek = dayOfWeek,
                    plans = plans,
                    recentCompletedPlanIds = completedSessions
                        .map { session -> session.planId }
                        .distinct()
                        .take(5),
                    primaryGoal = primaryGoal,
                    preferredWorkoutFrequency = preferredWorkoutFrequency,
                    experienceLevel = experienceLevel
                )
            )
        }.getOrNull()

        if (aiRecommendation != null) {
            val aiPlan = plans.firstOrNull { plan -> plan.id == aiRecommendation.planId }
            if (aiPlan != null) {
                return DailyWorkoutRecommendation(
                    plan = aiPlan,
                    reason = aiRecommendation.reason,
                    source = RecommendationSource.AI
                )
            }
        }

        val fallbackSelection = selectFallbackRecommendation(
            plans = plans,
            completedSessions = completedSessions,
            dayOfWeek = dayOfWeek,
            primaryGoal = primaryGoal,
            preferredWorkoutFrequency = preferredWorkoutFrequency
        )

        return DailyWorkoutRecommendation(
            plan = fallbackSelection.plan,
            reason = fallbackSelection.reason,
            source = RecommendationSource.SMART_FALLBACK
        )
    }

    private fun selectFallbackRecommendation(
        plans: List<WorkoutPlan>,
        completedSessions: List<WorkoutSession>,
        dayOfWeek: Int,
        primaryGoal: String,
        preferredWorkoutFrequency: Int
    ): FallbackSelection {
        val recentPlanIds = completedSessions.take(3).map { session -> session.planId }.toSet()
        val lastCompletedByPlan = completedSessions
            .groupBy { session -> session.planId }
            .mapValues { (_, sessions) ->
                sessions.maxOfOrNull { session -> session.endTime ?: session.startTime } ?: Long.MIN_VALUE
            }

        val scoredPlans = plans.map { plan ->
            buildPlanScore(
                plan = plan,
                recentPlanIds = recentPlanIds,
                lastCompletedByPlan = lastCompletedByPlan,
                dayOfWeek = dayOfWeek,
                primaryGoal = primaryGoal,
                preferredWorkoutFrequency = preferredWorkoutFrequency
            )
        }

        val selected = scoredPlans
            .sortedWith(
                compareByDescending<PlanScore> { planScore -> planScore.score }
                    .thenBy { planScore -> planScore.plan.name.lowercase() }
            )
            .first()

        return FallbackSelection(
            plan = selected.plan,
            reason = buildFallbackReason(selected)
        )
    }

    private fun buildPlanScore(
        plan: WorkoutPlan,
        recentPlanIds: Set<String>,
        lastCompletedByPlan: Map<String, Long>,
        dayOfWeek: Int,
        primaryGoal: String,
        preferredWorkoutFrequency: Int
    ): PlanScore {
        val recentPenalty = if (plan.id in recentPlanIds) -18 else 8
        val daysSinceLast = daysSinceLastCompleted(lastCompletedByPlan[plan.id])
        val recencyBonus = daysSinceLast.coerceIn(0, 30)

        val goalMatchCount = countGoalMatches(plan, primaryGoal)
        val goalBonus = when {
            goalMatchCount >= 2 -> 22
            goalMatchCount == 1 -> 12
            else -> 0
        }

        val durationScore = calculateDurationScore(
            estimatedDuration = plan.estimatedDuration,
            preferredWorkoutFrequency = preferredWorkoutFrequency
        )

        val dayBonus = when {
            dayOfWeek in 1..5 && plan.estimatedDuration in 20..50 -> 4
            dayOfWeek in 6..7 && plan.estimatedDuration >= 40 -> 3
            else -> 0
        }

        return PlanScore(
            plan = plan,
            score = recentPenalty + recencyBonus + goalBonus + durationScore + dayBonus,
            rotatedFromRecent = plan.id !in recentPlanIds,
            matchedGoal = goalMatchCount > 0,
            durationAligned = durationScore >= 10
        )
    }

    private fun daysSinceLastCompleted(lastCompletedAt: Long?): Int {
        if (lastCompletedAt == null || lastCompletedAt == Long.MIN_VALUE) {
            return 30
        }

        val elapsed = (System.currentTimeMillis() - lastCompletedAt).coerceAtLeast(0L)
        return (elapsed / MILLIS_PER_DAY).toInt()
    }

    private fun countGoalMatches(plan: WorkoutPlan, primaryGoal: String): Int {
        val searchableText = listOf(
            plan.name,
            plan.description,
            plan.targetMuscleGroups.joinToString(" "),
            plan.tags.joinToString(" ")
        ).joinToString(" ").lowercase()

        return goalTokens(primaryGoal).count { token ->
            val normalizedToken = token.lowercase()
            searchableText.contains(normalizedToken) ||
                searchableText.contains(normalizedToken.replace("_", " "))
        }
    }

    private fun goalTokens(primaryGoal: String): Set<String> {
        return when (primaryGoal.uppercase()) {
            "MUSCLE_GAIN" -> setOf("muscle_gain", "hypertrophy", "push", "pull", "legs", "upper", "lower")
            "FAT_LOSS" -> setOf("fat_loss", "conditioning", "cardio", "hiit", "full_body")
            "STRENGTH" -> setOf("strength", "power", "compound")
            else -> setOf("general_fitness", "full_body", "beginner", "mobility")
        }
    }

    private fun calculateDurationScore(estimatedDuration: Int, preferredWorkoutFrequency: Int): Int {
        if (estimatedDuration <= 0) {
            return 0
        }

        val targetDuration = when (preferredWorkoutFrequency.coerceIn(1, 7)) {
            1 -> 70
            2 -> 60
            3 -> 50
            4 -> 45
            5 -> 40
            6 -> 32
            else -> 28
        }

        val distance = abs(estimatedDuration - targetDuration)
        return (20 - (distance / 2)).coerceAtLeast(0)
    }

    private fun buildFallbackReason(selected: PlanScore): String {
        return when {
            selected.matchedGoal && selected.durationAligned && selected.rotatedFromRecent -> {
                "Chosen to match your goal, fit your weekly rhythm, and rotate recent training." 
            }

            selected.matchedGoal && selected.durationAligned -> {
                "Chosen to match your goal and fit your preferred weekly training rhythm." 
            }

            selected.matchedGoal -> {
                "Chosen to align with your current fitness goal while keeping training balanced." 
            }

            selected.rotatedFromRecent -> {
                "Chosen to rotate your recent training and keep weekly variety balanced." 
            }

            else -> {
                "Chosen as your most balanced option based on recent workout history." 
            }
        }
    }

    private companion object {
        const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
    }
}
