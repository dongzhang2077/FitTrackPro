package com.domcheung.fittrackpro.usecase

import com.domcheung.fittrackpro.data.local.UserPreferencesManager
import com.domcheung.fittrackpro.data.model.WorkoutPlan
import com.domcheung.fittrackpro.data.model.WorkoutSession
import com.domcheung.fittrackpro.data.model.WorkoutStatus
import com.domcheung.fittrackpro.data.recommendation.AiPlanRecommendation
import com.domcheung.fittrackpro.data.recommendation.AiWorkoutRecommendationService
import com.domcheung.fittrackpro.data.repository.WorkoutRepository
import com.domcheung.fittrackpro.domain.model.RecommendationSource
import com.domcheung.fittrackpro.domain.usecase.GetTodaysRecommendedPlanUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetTodaysRecommendedPlanUseCaseTest {

    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var aiWorkoutRecommendationService: AiWorkoutRecommendationService
    private lateinit var userPreferencesManager: UserPreferencesManager
    private lateinit var useCase: GetTodaysRecommendedPlanUseCase

    @Before
    fun setup() {
        workoutRepository = mock()
        aiWorkoutRecommendationService = mock()
        userPreferencesManager = mock()
        whenever(userPreferencesManager.fitnessPrimaryGoal).thenReturn(flowOf("GENERAL_FITNESS"))
        whenever(userPreferencesManager.fitnessWorkoutFrequency).thenReturn(flowOf(3))
        whenever(userPreferencesManager.fitnessExperienceLevel).thenReturn(flowOf("BEGINNER"))
        useCase = GetTodaysRecommendedPlanUseCase(
            workoutRepository = workoutRepository,
            aiWorkoutRecommendationService = aiWorkoutRecommendationService,
            userPreferencesManager = userPreferencesManager
        )
    }

    @Test
    fun returnsNoneWhenNoPlansExist() {
        runBlocking {
            whenever(workoutRepository.getUserWorkoutPlans("user-1")).thenReturn(flowOf(emptyList()))

            val result = useCase("user-1")

            assertNull(result.plan)
            assertEquals(RecommendationSource.NONE, result.source)
        }
    }

    @Test
    fun usesAiRecommendationWhenPlanIdIsValid() {
        runBlocking {
            val planA = samplePlan(id = "plan-a", name = "Push Day")
            val planB = samplePlan(id = "plan-b", name = "Pull Day")

            whenever(workoutRepository.getUserWorkoutPlans("user-1")).thenReturn(flowOf(listOf(planA, planB)))
            whenever(workoutRepository.getCompletedWorkoutSessions("user-1")).thenReturn(flowOf(emptyList()))
            whenever(aiWorkoutRecommendationService.recommendPlan(any())).thenReturn(
                AiPlanRecommendation(
                    planId = "plan-b",
                    reason = "Chosen to balance muscle focus from your recent routine."
                )
            )

            val result = useCase("user-1")

            assertEquals("plan-b", result.plan?.id)
            assertEquals(RecommendationSource.AI, result.source)
            verify(aiWorkoutRecommendationService).recommendPlan(any())
        }
    }

    @Test
    fun fallsBackToNonRecentPlanWhenAiUnavailable() {
        runBlocking {
            val planA = samplePlan(id = "plan-a", name = "Push Day")
            val planB = samplePlan(id = "plan-b", name = "Pull Day")

            whenever(workoutRepository.getUserWorkoutPlans("user-1")).thenReturn(flowOf(listOf(planA, planB)))
            whenever(workoutRepository.getCompletedWorkoutSessions("user-1")).thenReturn(
                flowOf(
                    listOf(
                        sampleCompletedSession(
                            sessionId = "session-1",
                            userId = "user-1",
                            plan = planA,
                            endedAt = 1_000L
                        )
                    )
                )
            )
            whenever(aiWorkoutRecommendationService.recommendPlan(any())).thenReturn(null)

            val result = useCase("user-1")

            assertEquals("plan-b", result.plan?.id)
            assertEquals(RecommendationSource.SMART_FALLBACK, result.source)
        }
    }

    @Test
    fun fallbackPrefersGoalMatchedTags() {
        runBlocking {
            whenever(userPreferencesManager.fitnessPrimaryGoal).thenReturn(flowOf("FAT_LOSS"))
            whenever(userPreferencesManager.fitnessWorkoutFrequency).thenReturn(flowOf(5))

            val planA = samplePlan(
                id = "plan-a",
                name = "Strength Upper",
                tags = listOf("goal_strength", "frequency_3")
            )
            val planB = samplePlan(
                id = "plan-b",
                name = "Conditioning Circuit",
                tags = listOf("goal_fat_loss", "conditioning", "frequency_5")
            )

            whenever(workoutRepository.getUserWorkoutPlans("user-1")).thenReturn(flowOf(listOf(planA, planB)))
            whenever(workoutRepository.getCompletedWorkoutSessions("user-1")).thenReturn(flowOf(emptyList()))
            whenever(aiWorkoutRecommendationService.recommendPlan(any())).thenReturn(null)

            val result = useCase("user-1")

            assertEquals("plan-b", result.plan?.id)
            assertEquals(RecommendationSource.SMART_FALLBACK, result.source)
        }
    }

    @Test
    fun fallbackPrefersShorterDurationForHighFrequencyPreference() {
        runBlocking {
            whenever(userPreferencesManager.fitnessWorkoutFrequency).thenReturn(flowOf(7))

            val planA = samplePlan(id = "plan-a", name = "Long Session", estimatedDuration = 70)
            val planB = samplePlan(id = "plan-b", name = "Quick Session", estimatedDuration = 25)

            whenever(workoutRepository.getUserWorkoutPlans("user-1")).thenReturn(flowOf(listOf(planA, planB)))
            whenever(workoutRepository.getCompletedWorkoutSessions("user-1")).thenReturn(flowOf(emptyList()))
            whenever(aiWorkoutRecommendationService.recommendPlan(any())).thenReturn(null)

            val result = useCase("user-1")

            assertEquals("plan-b", result.plan?.id)
            assertEquals(RecommendationSource.SMART_FALLBACK, result.source)
        }
    }

    private fun samplePlan(
        id: String,
        name: String,
        estimatedDuration: Int = 45,
        targetMuscleGroups: List<String> = listOf("Full Body"),
        tags: List<String> = emptyList(),
        description: String = ""
    ): WorkoutPlan {
        return WorkoutPlan(
            id = id,
            name = name,
            description = description,
            estimatedDuration = estimatedDuration,
            targetMuscleGroups = targetMuscleGroups,
            tags = tags
        )
    }

    private fun sampleCompletedSession(
        sessionId: String,
        userId: String,
        plan: WorkoutPlan,
        endedAt: Long
    ): WorkoutSession {
        return WorkoutSession(
            id = sessionId,
            planId = plan.id,
            planName = plan.name,
            originalPlan = plan,
            currentPlan = plan,
            userId = userId,
            startTime = endedAt - 1_800_000L,
            endTime = endedAt,
            status = WorkoutStatus.COMPLETED
        )
    }
}
