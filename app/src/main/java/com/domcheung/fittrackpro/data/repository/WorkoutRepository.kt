package com.domcheung.fittrackpro.data.repository

import com.domcheung.fittrackpro.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for workout-related data operations
 * Defines all data access methods used by the domain layer
 *
 * This interface abstracts data sources (Room + Firebase) from business logic
 * Implementation will handle local caching, sync, and offline capabilities
 */
interface WorkoutRepository {

    // ========== Exercise Operations ==========

    /**
     * Get all exercises with live updates
     */
    fun getAllExercises(): Flow<List<Exercise>>

    /**
     * Get exercise by ID
     */
    suspend fun getExerciseById(exerciseId: Int): Exercise?

    /**
     * Search exercises by name or muscle group
     */
    fun searchExercises(query: String): Flow<List<Exercise>>

    /**
     * Get exercises by category (muscle group)
     */
    fun getExercisesByCategory(category: String): Flow<List<Exercise>>

    /**
     * Get exercises by equipment type
     */
    fun getExercisesByEquipment(equipment: String): Flow<List<Exercise>>

    /**
     * Get user's custom exercises
     */
    fun getCustomExercises(): Flow<List<Exercise>>

    /**
     * Create custom exercise
     */
    suspend fun createCustomExercise(exercise: Exercise): Result<Long>

    /**
     * Update existing exercise
     */
    suspend fun updateExercise(exercise: Exercise): Result<Unit>

    /**
     * Delete custom exercise
     */
    suspend fun deleteCustomExercise(exerciseId: Int): Result<Unit>

    /**
     * Sync exercises from Wger API
     */
    suspend fun syncExercisesFromApi(): Result<Unit>

    // ========== Workout Plan Operations ==========

    /**
     * Get all workout plans for user
     */
    fun getUserWorkoutPlans(userId: String): Flow<List<WorkoutPlan>>

    /**
     * Get workout plan by ID
     */
    suspend fun getWorkoutPlanById(planId: String): WorkoutPlan?

    /**
     * Get workout plan with live updates
     */
    fun getWorkoutPlanByIdFlow(planId: String): Flow<WorkoutPlan?>

    /**
     * Get workout session by ID with live updates
     */
    fun getWorkoutSessionByIdFlow(sessionId: String): Flow<WorkoutSession?>

    /**
     * Get template workout plans
     */
    fun getTemplateWorkoutPlans(): Flow<List<WorkoutPlan>>

    /**
     * Search workout plans
     */
    fun searchWorkoutPlans(userId: String, query: String): Flow<List<WorkoutPlan>>

    /**
     * Create new workout plan
     */
    suspend fun createWorkoutPlan(workoutPlan: WorkoutPlan): Result<String>

    /**
     * Update existing workout plan
     */
    suspend fun updateWorkoutPlan(workoutPlan: WorkoutPlan): Result<Unit>

    /**
     * Delete workout plan
     */
    suspend fun deleteWorkoutPlan(planId: String): Result<Unit>

    /**
     * Copy workout plan (create duplicate)
     */
    suspend fun copyWorkoutPlan(planId: String, newName: String): Result<String>

    // ========== Workout Session Operations ==========

    /**
     * Get all workout sessions for user
     */
    fun getUserWorkoutSessions(userId: String): Flow<List<WorkoutSession>>

    /**
     * Get workout session by ID
     */
    suspend fun getWorkoutSessionById(sessionId: String): WorkoutSession?

    /**
     * Get active workout session (if any)
     */
    suspend fun getActiveWorkoutSession(userId: String): WorkoutSession?

    /**
     * Get active workout session with live updates
     */
    fun getActiveWorkoutSessionFlow(userId: String): Flow<WorkoutSession?>

    /**
     * Get completed workout sessions
     */
    fun getCompletedWorkoutSessions(userId: String): Flow<List<WorkoutSession>>

    /**
     * Get workout sessions by date range
     */
    fun getWorkoutSessionsByDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<WorkoutSession>>

    /**
     * Start new workout session
     */
    suspend fun startWorkoutSession(
        planId: String,
        userId: String
    ): Result<WorkoutSession>

    /**
     * Update workout session (save progress)
     */
    suspend fun updateWorkoutSession(workoutSession: WorkoutSession): Result<Unit>

    /**
     * Pause workout session
     */
    suspend fun pauseWorkoutSession(sessionId: String, isResting: Boolean): Result<Unit>

    /**
     * Resume workout session
     */
    suspend fun resumeWorkoutSession(sessionId: String): Result<Unit>

    /**
     * Complete workout session
     */
    suspend fun completeWorkoutSession(sessionId: String): Result<Unit>

    /**
     * Abandon workout session
     */
    suspend fun abandonWorkoutSession(sessionId: String): Result<Unit>

    // ========== Personal Record Operations ==========

    /**
     * Get all personal records for user
     */
    fun getUserPersonalRecords(userId: String): Flow<List<PersonalRecord>>

    /**
     * Get personal records for specific exercise
     */
    fun getPersonalRecordsByExercise(
        userId: String,
        exerciseId: Int
    ): Flow<List<PersonalRecord>>

    /**
     * Get best record for exercise by type
     */
    suspend fun getBestPersonalRecord(
        userId: String,
        exerciseId: Int,
        recordType: RecordType
    ): PersonalRecord?

    /**
     * Check and create personal record if applicable
     */
    suspend fun checkAndCreatePersonalRecord(
        userId: String,
        exerciseId: Int,
        weight: Float,
        reps: Int,
        sessionId: String
    ): Result<List<PersonalRecord>>

    /**
     * Get recent personal records (last 30 days)
     */
    fun getRecentPersonalRecords(userId: String): Flow<List<PersonalRecord>>

    // ========== Statistics and Analytics ==========

    /**
     * Get workout statistics for user
     */
    suspend fun getWorkoutStatistics(userId: String): Result<WorkoutStatistics>

    /**
     * Get monthly workout summary
     */
    suspend fun getMonthlyWorkoutSummary(
        userId: String,
        year: Int,
        month: Int
    ): Result<MonthlyWorkoutSummary>

    /**
     * Get current workout streak
     */
    suspend fun getCurrentWorkoutStreak(userId: String): Int

    /**
     * Get total volume lifted
     */
    suspend fun getTotalVolumeLifted(userId: String): Float

    // ========== Sync Operations ==========

    /**
     * Sync all user data to Firebase
     */
    suspend fun syncToFirebase(userId: String): Result<Unit>

    /**
     * Sync user data from Firebase
     */
    suspend fun syncFromFirebase(userId: String): Result<Unit>

    /**
     * Check if data needs sync
     */
    suspend fun hasUnsyncedData(): Boolean

    /**
     * Seeds initial data if necessary (e.g., for new users).
     */
    suspend fun seedInitialPlansIfEmpty(userId: String)
}

// ========== Data Transfer Objects ==========

/**
 * Workout statistics summary
 */
data class WorkoutStatistics(
    val totalWorkouts: Int,
    val totalWorkoutTime: Long, // milliseconds
    val averageWorkoutDuration: Long, // milliseconds
    val totalVolumeLifted: Float,
    val currentStreak: Int,
    val longestStreak: Int,
    val averageCompletionRate: Float,
    val favoriteExercises: List<String>,
    val totalPersonalRecords: Int
)

/**
 * Monthly workout summary
 */
data class MonthlyWorkoutSummary(
    val year: Int,
    val month: Int,
    val totalWorkouts: Int,
    val totalTime: Long,
    val totalVolume: Float,
    val averageCompletion: Float,
    val personalRecordsCount: Int,
    val mostUsedPlan: String?,
    val workoutDates: List<String>
)