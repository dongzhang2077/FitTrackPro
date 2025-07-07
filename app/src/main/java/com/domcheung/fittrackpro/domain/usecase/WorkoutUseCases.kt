package com.domcheung.fittrackpro.domain.usecase

import com.domcheung.fittrackpro.data.model.*
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

/**
 * Use case for creating workout plans
 * Validates and creates new workout plans
 */
@Singleton
class CreateWorkoutPlanUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        exercises: List<PlannedExercise>,
        userId: String,
        isTemplate: Boolean = true
    ): Result<String> {
        // Validate input
        if (name.isBlank()) {
            return Result.failure(Exception("Workout plan name cannot be empty"))
        }
        if (exercises.isEmpty()) {
            return Result.failure(Exception("Workout plan must contain at least one exercise"))
        }
        if (userId.isBlank()) {
            return Result.failure(Exception("User ID is required"))
        }

        val estimatedDuration = calculateEstimatedDuration(exercises)
        val targetMuscleGroups = extractTargetMuscleGroups(exercises)

        val workoutPlan = WorkoutPlan(
            id = "", // Will be generated in repository
            name = name.trim(),
            description = description.trim(),
            targetMuscleGroups = targetMuscleGroups,
            estimatedDuration = estimatedDuration,
            exercises = exercises,
            createdBy = userId,
            isTemplate = isTemplate
        )

        return repository.createWorkoutPlan(workoutPlan)
    }

    private fun calculateEstimatedDuration(exercises: List<PlannedExercise>): Int {
        // Rough estimation: 3 minutes per set + rest time
        val totalSets = exercises.sumOf { it.sets.size }
        val totalRestTime = exercises.sumOf { exercise ->
            exercise.sets.sumOf { it.restAfter.toInt() }
        } / 60 // Convert to minutes

        return (totalSets * 3) + totalRestTime
    }

    private fun extractTargetMuscleGroups(exercises: List<PlannedExercise>): List<String> {
        // This would typically require querying exercise details
        // For now, return empty list - to be implemented with exercise lookup
        return emptyList()
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
 * Use case for updating workout session progress
 * Handles real-time workout progress updates
 */
@Singleton
class UpdateWorkoutProgressUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        exerciseId: Int,
        setNumber: Int,
        weight: Float,
        reps: Int,
        userId: String
    ): Result<PersonalRecord?> {
        // Get current session
        val session = repository.getWorkoutSessionById(sessionId)
            ?: return Result.failure(Exception("Workout session not found"))

        // Update session progress
        val updatedSession = updateSessionProgress(session, exerciseId, setNumber, weight, reps)
        val updateResult = repository.updateWorkoutSession(updatedSession)

        if (updateResult.isFailure) {
            return Result.failure(updateResult.exceptionOrNull() ?: Exception("Failed to update session"))
        }

        // Check for personal records
        return repository.checkAndCreatePersonalRecord(userId, exerciseId, weight, reps, sessionId)
    }

    private fun updateSessionProgress(
        session: WorkoutSession,
        exerciseId: Int,
        setNumber: Int,
        weight: Float,
        reps: Int
    ): WorkoutSession {
        val updatedExercises = session.exercises.map { exercise ->
            if (exercise.exerciseId == exerciseId) {
                val updatedSets = exercise.executedSets.toMutableList()
                val setIndex = updatedSets.indexOfFirst { it.setNumber == setNumber }

                if (setIndex >= 0) {
                    updatedSets[setIndex] = updatedSets[setIndex].copy(
                        actualWeight = weight,
                        actualReps = reps,
                        isCompleted = true,
                        completedAt = System.currentTimeMillis()
                    )
                } else {
                    // Add new set if not exists
                    updatedSets.add(
                        ExecutedSet(
                            setNumber = setNumber,
                            plannedWeight = weight,
                            plannedReps = reps,
                            actualWeight = weight,
                            actualReps = reps,
                            isCompleted = true,
                            completedAt = System.currentTimeMillis()
                        )
                    )
                }

                exercise.copy(executedSets = updatedSets)
            } else {
                exercise
            }
        }

        val completionPercentage = calculateCompletionPercentage(updatedExercises)
        val totalVolume = calculateTotalVolume(updatedExercises)

        return session.copy(
            exercises = updatedExercises,
            completionPercentage = completionPercentage,
            totalVolume = totalVolume
        )
    }

    private fun calculateCompletionPercentage(exercises: List<ExecutedExercise>): Float {
        if (exercises.isEmpty()) return 0f

        val totalPlannedSets = exercises.sumOf { it.plannedSets.size }
        val totalCompletedSets = exercises.sumOf { exercise ->
            exercise.executedSets.count { it.isCompleted }
        }

        return if (totalPlannedSets > 0) {
            (totalCompletedSets.toFloat() / totalPlannedSets.toFloat()) * 100f
        } else {
            0f
        }
    }

    private fun calculateTotalVolume(exercises: List<ExecutedExercise>): Float {
        return exercises.sumOf { exercise ->
            exercise.executedSets.filter { it.isCompleted }.sumOf { set ->
                (set.actualWeight * set.actualReps).toDouble()
            }
        }.toFloat()
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

// ========== Data Management Use Cases ==========

/**
 * Use case for syncing data
 * Handles data synchronization between local and remote storage
 */
@Singleton
class SyncDataUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        return try {
            // Check if sync is needed
            val hasUnsyncedData = repository.hasUnsyncedData()
            if (hasUnsyncedData) {
                repository.syncToFirebase(userId)
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasUnsyncedData(): Boolean {
        return repository.hasUnsyncedData()
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