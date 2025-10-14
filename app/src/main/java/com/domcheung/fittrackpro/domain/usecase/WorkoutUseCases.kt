package com.domcheung.fittrackpro.domain.usecase

import com.domcheung.fittrackpro.data.model.Exercise
import com.domcheung.fittrackpro.data.model.PersonalRecord
import com.domcheung.fittrackpro.data.model.PlannedExercise
import com.domcheung.fittrackpro.data.model.WorkoutPlan
import com.domcheung.fittrackpro.data.model.WorkoutSession
import com.domcheung.fittrackpro.data.repository.WorkoutRepository
import com.domcheung.fittrackpro.data.repository.WorkoutStatistics
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Collection of workout-related use cases
 * Contains business logic for workout operations
 * Each use case represents a specific business operation
 */

// ========== Exercise Use Cases ==========

/**
 * Use case for syncing exercises from Wger API
 * Handles fetching and storing exercises from external API
 */
@Singleton
class SyncExercisesFromApiUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.syncExercisesFromApi()
    }
}

/**
 * Use case for getting a single exercise by ID
 * Retrieves detailed exercise information
 */
@Singleton
class GetExerciseByIdUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(exerciseId: Int): Exercise? {
        return repository.getExerciseById(exerciseId)
    }
}

/**
 * Use case for searching exercises
 * Handles exercise filtering and search logic
 */
@Singleton
class SearchExercisesUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(query: String): Flow<List<Exercise>> {
        return if (query.isBlank()) {
            repository.getAllExercises()
        } else {
            repository.searchExercises(query.trim())
        }
    }
}

/**
 * Use case for getting exercises by category
 * Provides filtered exercises for specific muscle groups
 */
@Singleton
class GetExercisesByCategoryUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(category: String): Flow<List<Exercise>> {
        return repository.getExercisesByCategory(category)
    }
}

/**
 * Use case for creating custom exercises
 * Validates and creates user-defined exercises
 */
@Singleton
class CreateCustomExerciseUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        category: String,
        muscles: List<String>,
        equipment: List<String>
    ): Result<Long> {
        // Validate input
        if (name.isBlank()) {
            return Result.failure(Exception("Exercise name cannot be empty"))
        }

        val exercise = Exercise(
            id = System.currentTimeMillis().toInt(), // Temporary ID for custom exercises
            name = name.trim(),
            description = description.trim(),
            category = category,
            muscles = muscles,
            equipment = equipment,
            isCustom = true
        )

        return repository.createCustomExercise(exercise)
    }
}

// ========== Workout Plan Use Cases ==========

// In WorkoutUseCases.kt

/**
 * Use case for creating a new workout plan.
 * It takes a complete WorkoutPlan object, enriches it with calculated data,
 * and passes it to the repository for creation.
 */
@Singleton
class CreateWorkoutPlanUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    /**
     * @param workoutPlan The plan object created in the ViewModel.
     * @return A Result containing the ID of the newly created plan.
     */
    suspend operator fun invoke(workoutPlan: WorkoutPlan): Result<String> {
        // Validation can be done here before passing to the repository
        if (workoutPlan.name.isBlank()) {
            return Result.failure(Exception("Plan name cannot be empty."))
        }
        if (workoutPlan.exercises.isEmpty()) {
            return Result.failure(Exception("Plan must have at least one exercise."))
        }

        // Use the helper functions to calculate and enrich the plan object
        val estimatedDuration = calculateEstimatedDuration(workoutPlan.exercises)
        // val targetMuscleGroups = extractTargetMuscleGroups(workoutPlan.exercises) // This can be added later

        // Create the final plan object to be saved
        val finalPlan = workoutPlan.copy(
            estimatedDuration = estimatedDuration,
            // targetMuscleGroups = targetMuscleGroups
        )

        return repository.createWorkoutPlan(finalPlan)
    }

    /**
     * Calculates the estimated duration of a workout plan in minutes.
     * @param exercises The list of planned exercises.
     * @return The total estimated duration in minutes.
     */
    private fun calculateEstimatedDuration(exercises: List<PlannedExercise>): Int {
        // Rough estimation: 1.5 minutes per set (including rest)
        val totalSets = exercises.sumOf { it.sets.size }
        return (totalSets * 1.5).toInt()
    }

    /**
     * Extracts a unique list of muscle groups from the exercises in a plan.
     * NOTE: This requires that the Exercise objects are fully detailed.
     * This can be implemented in a future step.
     */
    private fun extractTargetMuscleGroups(exercises: List<PlannedExercise>): List<String> {
        // return exercises.flatMap { it.muscles }.distinct()
        return emptyList() // Keep it simple for now
    }
}

/**
 * Use case for getting user's workout plans
 * Provides filtered workout plans for a specific user
 */
@Singleton
class GetUserWorkoutPlansUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(userId: String): Flow<List<WorkoutPlan>> {
        return repository.getUserWorkoutPlans(userId)
    }
}

/**
 * Use case for copying workout plans
 * Creates duplicates of existing plans with modifications
 */
@Singleton
class CopyWorkoutPlanUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(planId: String, newName: String): Result<String> {
        if (newName.isBlank()) {
            return Result.failure(Exception("New plan name cannot be empty"))
        }
        return repository.copyWorkoutPlan(planId, newName.trim())
    }
}

// ========== Workout Session Use Cases ==========

/**
 * Use case for starting workout sessions
 * Handles workout session initialization and validation
 */
@Singleton
class StartWorkoutSessionUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(planId: String, userId: String): Result<WorkoutSession> {
        // Check if user already has an active session
        val activeSession = repository.getActiveWorkoutSession(userId)
        if (activeSession != null) {
            return Result.failure(Exception("You already have an active workout session. Please finish or abandon it first."))
        }

        return repository.startWorkoutSession(planId, userId)
    }
}

/**
 * Use case for getting active workout session
 * Provides current active session for resume functionality
 */
@Singleton
class GetActiveWorkoutSessionUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(userId: String): WorkoutSession? {
        return repository.getActiveWorkoutSession(userId)
    }

    fun flow(userId: String): Flow<WorkoutSession?> {
        return repository.getActiveWorkoutSessionFlow(userId)
    }
}


/**
 * Use case for completing workout sessions
 * Handles workout session completion and cleanup
 */
@Singleton
class CompleteWorkoutSessionUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(sessionId: String): Result<Unit> {
        return repository.completeWorkoutSession(sessionId)
    }
}

/**
 * Use case for pausing workout sessions
 * Handles workout session pause functionality
 */
@Singleton
class PauseWorkoutSessionUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    /**
     * Invokes the use case to pause a workout session.
     * @param sessionId The ID of the session to pause.
     * @param isResting True if this pause is for a rest period, false if it's a manual user pause.
     */
    suspend operator fun invoke(sessionId: String, isResting: Boolean): Result<Unit> {
        return repository.pauseWorkoutSession(sessionId, isResting)
    }
}

/**
 * Use case for resuming workout sessions
 * Handles workout session resume functionality
 */
@Singleton
class ResumeWorkoutSessionUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(sessionId: String): Result<Unit> {
        return repository.resumeWorkoutSession(sessionId)
    }
}

// ========== Statistics Use Cases ==========

/**
 * Use case for getting workout statistics
 * Provides comprehensive workout analytics
 */
@Singleton
class GetWorkoutStatisticsUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(userId: String): Result<WorkoutStatistics> {
        return repository.getWorkoutStatistics(userId)
    }
}

/**
 * Use case for getting personal records
 * Provides filtered personal records with analysis
 */
@Singleton
class GetPersonalRecordsUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    fun getAllRecords(userId: String): Flow<List<PersonalRecord>> {
        return repository.getUserPersonalRecords(userId)
    }

    fun getRecordsByExercise(userId: String, exerciseId: Int): Flow<List<PersonalRecord>> {
        return repository.getPersonalRecordsByExercise(userId, exerciseId)
    }

    fun getRecentRecords(userId: String): Flow<List<PersonalRecord>> {
        return repository.getRecentPersonalRecords(userId)
    }
}

@Singleton
class GetWorkoutSessionUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(sessionId: String): WorkoutSession? {
        return repository.getWorkoutSessionById(sessionId)
    }
}

@Singleton
class AbandonWorkoutSessionUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(sessionId: String): Result<Unit> {
        return repository.abandonWorkoutSession(sessionId)
    }
}

@Singleton
class GetWorkoutSessionByIdFlowUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(sessionId: String): Flow<WorkoutSession?> {
        return repository.getWorkoutSessionByIdFlow(sessionId)
    }
}

@Singleton
class UpdateWorkoutSessionUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    /**
     * Invokes the use case to update a given workout session.
     * This is typically used to save progress or any changes during the session.
     */
    suspend operator fun invoke(session: WorkoutSession): Result<Unit> {
        return repository.updateWorkoutSession(session)
    }
}


@Singleton
class CheckAndCreatePersonalRecordUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    /**
     * Invokes the use case to check for and create any new personal records for a given set.
     * @return A Result containing a List of new PersonalRecords. The list will be empty if no new records were set.
     */
    suspend operator fun invoke(
        userId: String,
        exerciseId: Int,
        exerciseName: String,
        weight: Float,
        reps: Int,
        sessionId: String
    ): Result<List<PersonalRecord>> { // <<< This return type is now corrected to match the repository
        // Basic validation: A PR can only be set with positive weight and reps.
        if (weight <= 0 || reps <= 0) {
            return Result.success(emptyList()) // Return an empty list for invalid performance.
        }
        return repository.checkAndCreatePersonalRecord(
            userId = userId,
            exerciseId = exerciseId,
            exerciseName = exerciseName,
            weight = weight,
            reps = reps,
            sessionId = sessionId
        )
    }
}


/**
 * Use case to sync exercises from the remote API or create sample ones if needed.
 * This should be called on app startup.
 */
@Singleton
class SyncExercisesUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.syncExercisesFromApi()
    }
}


// Add this new UseCase to your WorkoutUseCases.kt file

/**
 * Use case for deleting a workout plan.
 */
@Singleton
class DeleteWorkoutPlanUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    /**
     * @param planId The ID of the plan to be deleted.
     * @return A Result indicating success or failure.
     */
    suspend operator fun invoke(planId: String): Result<Unit> {
        return repository.deleteWorkoutPlan(planId)
    }
}


/**
 * Use case for getting all exercises from the repository.
 */
@Singleton
class GetAllExercisesUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(): Flow<List<Exercise>> {
        return repository.getAllExercises()
    }
}

/**
 * Use case for getting a list of exercises by their specific IDs.
 */
@Singleton
class GetExercisesByIdsUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(ids: List<Int>): List<Exercise> {
        return repository.getExercisesByIds(ids)
    }
}
