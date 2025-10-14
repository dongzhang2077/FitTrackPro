package com.domcheung.fittrackpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.domcheung.fittrackpro.data.model.Exercise
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Exercise entity
 * Handles all exercise-related database operations including Wger API sync
 */
@Dao
interface ExerciseDao {

    // ========== Query Operations ==========

    /**
     * Get all exercises with live updates
     */
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    /**
     * Get all exercises with images prioritized (images first, then alphabetically)
     */
    @Query("SELECT * FROM exercises ORDER BY CASE WHEN imageUrl IS NOT NULL THEN 0 ELSE 1 END, name ASC")
    fun getAllExercisesPrioritizeImages(): Flow<List<Exercise>>

    /**
     * Get exercises by muscle group category
     */
    @Query("SELECT * FROM exercises WHERE category = :category ORDER BY CASE WHEN imageUrl IS NOT NULL THEN 0 ELSE 1 END, name ASC")
    fun getExercisesByCategory(category: String): Flow<List<Exercise>>

    /**
     * Search exercises by name (case insensitive) with images prioritized
     */
    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY CASE WHEN imageUrl IS NOT NULL THEN 0 ELSE 1 END, name ASC")
    fun searchExercises(query: String): Flow<List<Exercise>>

    /**
     * Get exercise by ID
     */
    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: Int): Exercise?

    /**
     * Get multiple exercises by IDs
     */
    @Query("SELECT * FROM exercises WHERE id IN (:exerciseIds)")
    suspend fun getExercisesByIds(exerciseIds: List<Int>): List<Exercise>

    /**
     * Get exercises by equipment type
     */
    @Query("SELECT * FROM exercises WHERE equipment LIKE '%' || :equipment || '%' ORDER BY name ASC")
    fun getExercisesByEquipment(equipment: String): Flow<List<Exercise>>

    /**
     * Get custom exercises created by user
     */
    @Query("SELECT * FROM exercises WHERE isCustom = 1 ORDER BY createdAt DESC")
    fun getCustomExercises(): Flow<List<Exercise>>

    /**
     * Get exercises not synced to Firebase
     */
    @Query("SELECT * FROM exercises WHERE syncedToFirebase = 0")
    suspend fun getUnsyncedExercises(): List<Exercise>

    /**
     * Get total exercise count
     */
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int

    /**
     * Check if exercise exists by ID
     */
    @Query("SELECT EXISTS(SELECT 1 FROM exercises WHERE id = :exerciseId)")
    suspend fun exerciseExists(exerciseId: Int): Boolean

    // ========== Insert Operations ==========

    /**
     * Insert single exercise
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    /**
     * Insert multiple exercises (for API data sync)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>)

    // ========== Update Operations ==========

    /**
     * Update existing exercise
     */
    @Update
    suspend fun updateExercise(exercise: Exercise)

    /**
     * Mark exercise as synced to Firebase
     */
    @Query("UPDATE exercises SET syncedToFirebase = 1 WHERE id = :exerciseId")
    suspend fun markAsSynced(exerciseId: Int)

    /**
     * Mark multiple exercises as synced
     */
    @Query("UPDATE exercises SET syncedToFirebase = 1 WHERE id IN (:exerciseIds)")
    suspend fun markMultipleAsSynced(exerciseIds: List<Int>)

    // ========== Delete Operations ==========

    /**
     * Delete exercise by ID
     */
    @Query("DELETE FROM exercises WHERE id = :exerciseId")
    suspend fun deleteExerciseById(exerciseId: Int)

    /**
     * Delete custom exercise (only user-created exercises can be deleted)
     */
    @Query("DELETE FROM exercises WHERE id = :exerciseId AND isCustom = 1")
    suspend fun deleteCustomExercise(exerciseId: Int)

    /**
     * Clear all exercises from Wger API (for fresh sync, keeps custom exercises)
     */
    @Query("DELETE FROM exercises WHERE isCustom = 0")
    suspend fun clearApiExercises()

    /**
     * Delete all exercises (for testing/reset purposes)
     */
    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()

    // ========== Advanced Queries ==========

    /**
     * Get exercises by multiple muscle groups
     */
    @Query("""
        SELECT * FROM exercises 
        WHERE muscles LIKE '%' || :muscleGroup1 || '%' 
        OR muscles LIKE '%' || :muscleGroup2 || '%'
        ORDER BY name ASC
    """)
    fun getExercisesByMuscleGroups(muscleGroup1: String, muscleGroup2: String): Flow<List<Exercise>>

    /**
     * Get popular exercises (those used in workout plans)
     * Note: This would require a join with workout plans in real implementation
     */
    @Query("SELECT * FROM exercises WHERE isCustom = 0 ORDER BY name ASC LIMIT :limit")
    suspend fun getPopularExercises(limit: Int = 20): List<Exercise>

    /**
     * Get exercises that need images/videos (missing media content)
     */
    @Query("SELECT * FROM exercises WHERE (imageUrl IS NULL OR imageUrl = '') AND isCustom = 0")
    suspend fun getExercisesNeedingMedia(): List<Exercise>

    /**
     * Get exercises by difficulty level (based on equipment complexity)
     */
    @Query("SELECT * FROM exercises WHERE equipment = '' ORDER BY name ASC")
    fun getBodyweightExercises(): Flow<List<Exercise>>

    /**
     * Search exercises with advanced filters
     */
    @Query("""
        SELECT * FROM exercises 
        WHERE name LIKE '%' || :query || '%' 
        AND (:category = '' OR category = :category)
        AND (:hasEquipment = 0 OR (equipment != '' AND equipment IS NOT NULL))
        ORDER BY 
            CASE WHEN name LIKE :query || '%' THEN 1 ELSE 2 END,
            name ASC
    """)
    fun searchExercisesAdvanced(
        query: String,
        category: String = "",
        hasEquipment: Boolean = false
    ): Flow<List<Exercise>>
}