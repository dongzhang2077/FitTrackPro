package com.domcheung.fittrackpro.repository

import com.domcheung.fittrackpro.data.local.dao.ExerciseDao
import com.domcheung.fittrackpro.data.local.dao.PersonalRecordDao
import com.domcheung.fittrackpro.data.local.dao.WorkoutPlanDao
import com.domcheung.fittrackpro.data.local.dao.WorkoutSessionDao
import com.domcheung.fittrackpro.data.model.WorkoutPlan
import com.domcheung.fittrackpro.data.remote.WgerApiService
import com.domcheung.fittrackpro.data.repository.WorkoutRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class WorkoutRepositoryTest {

    private lateinit var exerciseDao: ExerciseDao
    private lateinit var workoutPlanDao: WorkoutPlanDao
    private lateinit var workoutSessionDao: WorkoutSessionDao
    private lateinit var personalRecordDao: PersonalRecordDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var wgerApiService: WgerApiService
    private lateinit var repository: WorkoutRepositoryImpl

    @Before
    fun setup() {
        exerciseDao = mock()
        workoutPlanDao = mock()
        workoutSessionDao = mock()
        personalRecordDao = mock()
        firestore = mock()
        wgerApiService = mock()

        repository = WorkoutRepositoryImpl(
            exerciseDao = exerciseDao,
            workoutPlanDao = workoutPlanDao,
            workoutSessionDao = workoutSessionDao,
            personalRecordDao = personalRecordDao,
            firestore = firestore,
            wgerApiService = wgerApiService
        )
    }

    @Test
    fun seedInitialPlansIfEmpty_insertsEightStarterTemplatesForNewUser() = runBlocking {
        whenever(workoutPlanDao.getUserWorkoutPlans("user-1")).thenReturn(flowOf(emptyList()))

        repository.seedInitialPlansIfEmpty("user-1")

        val plansCaptor = argumentCaptor<List<com.domcheung.fittrackpro.data.model.WorkoutPlan>>()
        verify(workoutPlanDao).insertWorkoutPlans(plansCaptor.capture())

        val seededPlans = plansCaptor.firstValue
        assertEquals(8, seededPlans.size)
        assertTrue(seededPlans.all { plan -> plan.isTemplate })
        assertTrue(seededPlans.all { plan -> plan.createdBy == "user-1" })

        val uniqueNames = seededPlans.map { plan -> plan.name }.toSet()
        assertEquals(seededPlans.size, uniqueNames.size)

        val allTags = seededPlans.flatMap { plan -> plan.tags }.toSet()
        assertTrue(allTags.contains("goal_general_fitness"))
        assertTrue(allTags.contains("goal_muscle_gain"))
        assertTrue(allTags.contains("goal_fat_loss"))
        assertTrue(allTags.contains("goal_strength"))
        assertTrue(allTags.contains("frequency_2"))
        assertTrue(allTags.contains("frequency_7"))
        assertTrue(allTags.contains("intensity_light"))
        assertTrue(allTags.contains("intensity_heavy"))
    }

    @Test
    fun seedInitialPlansIfEmpty_backfillsTemplatesWhenUserOnlyHasCustomPlans() = runBlocking {
        val existingCustomPlan = WorkoutPlan(
            id = "custom-1",
            name = "My Custom Plan",
            createdBy = "user-1",
            isTemplate = false
        )
        whenever(workoutPlanDao.getUserWorkoutPlans("user-1")).thenReturn(
            flowOf(listOf(existingCustomPlan))
        )

        repository.seedInitialPlansIfEmpty("user-1")

        val plansCaptor = argumentCaptor<List<WorkoutPlan>>()
        verify(workoutPlanDao).insertWorkoutPlans(plansCaptor.capture())
        assertEquals(8, plansCaptor.firstValue.size)
    }

    @Test
    fun seedInitialPlansIfEmpty_doesNotDuplicateWhenStarterTemplatesAlreadyExist() = runBlocking {
        val existingStarterTemplates = listOf(
            "Full Body Starter Loop",
            "Push Hypertrophy Base",
            "Pull Back Builder",
            "Leg Strength Foundation",
            "Fat Loss Conditioning Circuit",
            "Daily Light Full-Body",
            "Weekend Strength Builder",
            "Home Dumbbell Full-Body"
        ).mapIndexed { index, name ->
            WorkoutPlan(
                id = "template-$index",
                name = name,
                createdBy = "user-1",
                isTemplate = true
            )
        }
        whenever(workoutPlanDao.getUserWorkoutPlans("user-1")).thenReturn(
            flowOf(existingStarterTemplates)
        )

        repository.seedInitialPlansIfEmpty("user-1")

        verify(workoutPlanDao, never()).insertWorkoutPlans(any())
    }
}
