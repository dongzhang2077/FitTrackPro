package com.domcheung.fittrackpro.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.domcheung.fittrackpro.data.model.WorkoutPlan

/**
 * Data Access Object for WorkoutPlan entity
 * Handles all workout plan-related database operations including templates and user plans
 */
@Dao
interface WorkoutPlanDao {

    // ========== Query Operations ==========

    /**
     * Get all workout plans with live updates
     */
    @Query("SELECT * FROM workout_plans ORDER BY updatedAt DESC")
    fun getAllWorkoutPlans(): Flow<List<WorkoutPlan>>

    /**
     * Get workout plans created by specific user
     */
    @Query("SELECT * FROM workout_plans WHERE createdBy = :userId ORDER BY updatedAt DESC")
    fun getUserWorkoutPlans(userId: String): Flow<List<WorkoutPlan>>

    /**
     * Get workout plan by ID
     */
    @Query("SELECT * FROM workout_plans WHERE id = :planId")
    suspend fun getWorkoutPlanById(planId: String): WorkoutPlan?

    /**
     * Get workout plan by ID with live updates
     */
    @Query("SELECT * FROM workout_plans WHERE id = :planId")
    fun getWorkoutPlanByIdFlow(planId: String): Flow<WorkoutPlan?>

    /**
     * Get template workout plans (reusable plans)
     */
    @Query("SELECT * FROM workout_plans WHERE isTemplate = 1 ORDER BY updatedAt DESC")
    fun getTemplateWorkoutPlans(): Flow<List<WorkoutPlan>>

    /**
     * Search workout plans by name
     */
    @Query("SELECT * FROM workout_plans WHERE name LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchWorkoutPlans(query: String): Flow<List<WorkoutPlan>>

    /**
     * Get workout plans by target muscle groups
     */
    @Query("SELECT * FROM workout_plans WHERE targetMuscleGroups LIKE '%' || :muscleGroup || '%' ORDER BY updatedAt DESC")
    fun getWorkoutPlansByMuscleGroup(muscleGroup: String): Flow<List<WorkoutPlan>>

    /**
     * Get workout plans by tags
     */
    @Query("SELECT * FROM workout_plans WHERE tags LIKE '%' || :tag || '%' ORDER BY updatedAt DESC")
    fun getWorkoutPlansByTag(tag: String): Flow<List<WorkoutPlan>>

    /**
     * Get recently created workout plans
     */
    @Query("SELECT * FROM workout_plans ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentWorkoutPlans(limit: Int = 5): Flow<List<WorkoutPlan>>

    /**
     * Get recently updated workout plans
     */
    @Query("SELECT * FROM workout_plans ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentlyUpdatedPlans(limit: Int = 10): Flow<List<WorkoutPlan>>

    /**
     * Check if workout plan exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM workout_plans WHERE id = :planId)")
    suspend fun workoutPlanExists(planId: String): Boolean

    /**
     * Get total workout plan count for user
     */
    @Query("SELECT COUNT(*) FROM workout_plans WHERE createdBy = :userId")
    suspend fun getUserWorkoutPlanCount(userId: String): Int

    /**
     * Get workout plans not synced to Firebase
     */
    @Query("SELECT * FROM workout_plans WHERE syncedToFirebase = 0")
    suspend fun getUnsyncedWorkoutPlans(): List<WorkoutPlan>

    // ========== Insert Operations ==========

    /**
     * Insert single workout plan
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutPlan(workoutPlan: WorkoutPlan)

    /**
     * Insert multiple workout plans
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutPlans(workoutPlans: List<WorkoutPlan>)

    // ========== Update Operations ==========

    /**
     * Update existing workout plan
     */
    @Update
    suspend fun updateWorkoutPlan(workoutPlan: WorkoutPlan)

    /**
     * Update workout plan's updated timestamp
     */
    @Query("UPDATE workout_plans SET updatedAt = :timestamp WHERE id = :planId")
    suspend fun updateTimestamp(planId: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Update workout plan name
     */
    @Query("UPDATE workout_plans SET name = :newName, updatedAt = :timestamp WHERE id = :planId")
    suspend fun updateWorkoutPlanName(
        planId: String,
        newName: String,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Update workout plan description
     */
    @Query("UPDATE workout_plans SET description = :newDescription, updatedAt = :timestamp WHERE id = :planId")
    suspend fun updateWorkoutPlanDescription(
        planId: String,
        newDescription: String,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Toggle template status
     */
    @Query("UPDATE workout_plans SET isTemplate = :isTemplate, updatedAt = :timestamp WHERE id = :planId")
    suspend fun updateTemplateStatus(
        planId: String,
        isTemplate: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Mark workout plan as synced to Firebase
     */
    @Query("UPDATE workout_plans SET syncedToFirebase = 1 WHERE id = :planId")
    suspend fun markAsSynced(planId: String)

    /**
     * Mark multiple workout plans as synced
     */
    @Query("UPDATE workout_plans SET syncedToFirebase = 1 WHERE id IN (:planIds)")
    suspend fun markMultipleAsSynced(planIds: List<String>)

    // ========== Delete Operations ==========

    /**
     * Delete workout plan by ID
     */
    @Query("DELETE FROM workout_plans WHERE id = :planId")
    suspend fun deleteWorkoutPlanById(planId: String)

    /**
     * Delete workout plans created by user
     */
    @Query("DELETE FROM workout_plans WHERE createdBy = :userId")
    suspend fun deleteUserWorkoutPlans(userId: String)

    /**
     * Delete all workout plans (for testing/reset purposes)
     */
    @Query("DELETE FROM workout_plans")
    suspend fun deleteAllWorkoutPlans()

    // ========== Advanced Queries ==========

    /**
     * Get workout plans by duration range
     */
    @Query("SELECT * FROM workout_plans WHERE estimatedDuration BETWEEN :minDuration AND :maxDuration ORDER BY estimatedDuration ASC")
    fun getWorkoutPlansByDuration(minDuration: Int, maxDuration: Int): Flow<List<WorkoutPlan>>

    /**
     * Get workout plans by multiple tags
     */
    @Query("""
        SELECT * FROM workout_plans 
        WHERE tags LIKE '%' || :tag1 || '%' 
        OR tags LIKE '%' || :tag2 || '%'
        ORDER BY updatedAt DESC
    """)
    fun getWorkoutPlansByTags(tag1: String, tag2: String): Flow<List<WorkoutPlan>>

    /**
     * Get workout plans created in date range
     */
    @Query("SELECT * FROM workout_plans WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    fun getWorkoutPlansInDateRange(startDate: Long, endDate: Long): Flow<List<WorkoutPlan>>

    /**
     * Search workout plans with advanced filters
     */
    @Query("""
        SELECT * FROM workout_plans 
        WHERE (:query = '' OR name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        AND (:userId = '' OR createdBy = :userId)
        AND (:isTemplate = -1 OR isTemplate = :isTemplate)
        AND (:minDuration = 0 OR estimatedDuration >= :minDuration)
        AND (:maxDuration = 0 OR estimatedDuration <= :maxDuration)
        ORDER BY 
            CASE WHEN name LIKE :query || '%' THEN 1 ELSE 2 END,
            updatedAt DESC
    """)
    fun searchWorkoutPlansAdvanced(
        query: String = "",
        userId: String = "",
        isTemplate: Int = -1, // -1 = all, 0 = false, 1 = true
        minDuration: Int = 0,
        maxDuration: Int = 0
    ): Flow<List<WorkoutPlan>>

    /**
     * Get favorite workout plans (most recently used)
     * Note: This would ideally join with workout_sessions table to get actual usage data
     */
    @Query("SELECT * FROM workout_plans WHERE createdBy = :userId ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getFavoriteWorkoutPlans(userId: String, limit: Int = 5): List<WorkoutPlan>

    /**
     * Get workout plans that need sync (modified locally)
     */
    @Query("SELECT * FROM workout_plans WHERE syncedToFirebase = 0 AND updatedAt > createdAt")
    suspend fun getModifiedPlansNeedingSync(): List<WorkoutPlan>

    /**
     * Get total workout plan count for user (simplified)
     */
    @Query("SELECT COUNT(*) FROM workout_plans WHERE createdBy = :userId")
    suspend fun getUserPlanTotalCount(userId: String): Int

    /**
     * Get template count for user
     */
    @Query("SELECT COUNT(*) FROM workout_plans WHERE createdBy = :userId AND isTemplate = 1")
    suspend fun getUserTemplateCount(userId: String): Int

    /**
     * Get average duration for user plans
     */
    @Query("SELECT AVG(estimatedDuration) FROM workout_plans WHERE createdBy = :userId")
    suspend fun getUserPlanAverageDuration(userId: String): Double?
}