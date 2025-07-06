package com.domcheung.fittrackpro.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.domcheung.fittrackpro.data.model.WorkoutSession
import com.domcheung.fittrackpro.data.model.WorkoutStatus

/**
 * Data Access Object for WorkoutSession entity
 * Handles all workout session-related database operations including real-time execution tracking
 */
@Dao
interface WorkoutSessionDao {

    // ========== Query Operations ==========

    /**
     * Get all workout sessions with live updates
     */
    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllWorkoutSessions(): Flow<List<WorkoutSession>>

    /**
     * Get workout sessions for specific user
     */
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun getUserWorkoutSessions(userId: String): Flow<List<WorkoutSession>>

    /**
     * Get workout session by ID
     */
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getWorkoutSessionById(sessionId: String): WorkoutSession?

    /**
     * Get workout session by ID with live updates
     */
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    fun getWorkoutSessionByIdFlow(sessionId: String): Flow<WorkoutSession?>

    /**
     * Get current active workout session (IN_PROGRESS or PAUSED)
     * Critical for resuming workouts and preventing multiple active sessions
     */
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND (status = 'IN_PROGRESS' OR status = 'PAUSED') ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveWorkoutSession(userId: String): WorkoutSession?

    /**
     * Get active workout session with live updates
     */
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND (status = 'IN_PROGRESS' OR status = 'PAUSED') ORDER BY startTime DESC LIMIT 1")
    fun getActiveWorkoutSessionFlow(userId: String): Flow<WorkoutSession?>

    /**
     * Get completed workout sessions
     */
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND status = 'COMPLETED' ORDER BY startTime DESC")
    fun getCompletedWorkoutSessions(userId: String): Flow<List<WorkoutSession>>

    /**
     * Get workout sessions by plan ID
     */
    @Query("SELECT * FROM workout_sessions WHERE planId = :planId ORDER BY startTime DESC")
    fun getWorkoutSessionsByPlan(planId: String): Flow<List<WorkoutSession>>

    /**
     * Get workout sessions by date range
     */
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND startTime BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    fun getWorkoutSessionsByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<WorkoutSession>>

    /**
     * Get recent workout sessions
     */
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId ORDER BY startTime DESC LIMIT :limit")
    fun getRecentWorkoutSessions(userId: String, limit: Int = 10): Flow<List<WorkoutSession>>

    /**
     * Get workout sessions by status
     */
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND status = :status ORDER BY startTime DESC")
    fun getWorkoutSessionsByStatus(userId: String, status: WorkoutStatus): Flow<List<WorkoutSession>>

    /**
     * Check if there's an active workout session
     */
    @Query("SELECT EXISTS(SELECT 1 FROM workout_sessions WHERE userId = :userId AND (status = 'IN_PROGRESS' OR status = 'PAUSED'))")
    suspend fun hasActiveWorkoutSession(userId: String): Boolean

    /**
     * Get workout sessions not synced to Firebase
     */
    @Query("SELECT * FROM workout_sessions WHERE syncedToFirebase = 0")
    suspend fun getUnsyncedWorkoutSessions(): List<WorkoutSession>

    /**
     * Get total workout session count for user
     */
    @Query("SELECT COUNT(*) FROM workout_sessions WHERE userId = :userId")
    suspend fun getUserWorkoutSessionCount(userId: String): Int

    /**
     * Get completed workout session count for user
     */
    @Query("SELECT COUNT(*) FROM workout_sessions WHERE userId = :userId AND status = 'COMPLETED'")
    suspend fun getUserCompletedWorkoutSessionCount(userId: String): Int

    // ========== Insert Operations ==========

    /**
     * Insert single workout session
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSession(workoutSession: WorkoutSession)

    /**
     * Insert multiple workout sessions
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSessions(workoutSessions: List<WorkoutSession>)

    // ========== Update Operations ==========

    /**
     * Update existing workout session
     */
    @Update
    suspend fun updateWorkoutSession(workoutSession: WorkoutSession)

    /**
     * Update workout session status
     */
    @Query("UPDATE workout_sessions SET status = :status WHERE id = :sessionId")
    suspend fun updateWorkoutSessionStatus(sessionId: String, status: WorkoutStatus)

    /**
     * Update workout session end time
     */
    @Query("UPDATE workout_sessions SET endTime = :endTime WHERE id = :sessionId")
    suspend fun updateWorkoutSessionEndTime(sessionId: String, endTime: Long)

    /**
     * Update paused duration
     */
    @Query("UPDATE workout_sessions SET pausedDuration = :pausedDuration WHERE id = :sessionId")
    suspend fun updatePausedDuration(sessionId: String, pausedDuration: Long)

    /**
     * Update completion percentage
     */
    @Query("UPDATE workout_sessions SET completionPercentage = :percentage WHERE id = :sessionId")
    suspend fun updateCompletionPercentage(sessionId: String, percentage: Float)

    /**
     * Update total volume
     */
    @Query("UPDATE workout_sessions SET totalVolume = :volume WHERE id = :sessionId")
    suspend fun updateTotalVolume(sessionId: String, volume: Float)

    /**
     * Mark workout session as synced to Firebase
     */
    @Query("UPDATE workout_sessions SET syncedToFirebase = 1 WHERE id = :sessionId")
    suspend fun markAsSynced(sessionId: String)

    /**
     * Mark multiple workout sessions as synced
     */
    @Query("UPDATE workout_sessions SET syncedToFirebase = 1 WHERE id IN (:sessionIds)")
    suspend fun markMultipleAsSynced(sessionIds: List<String>)

    // ========== Delete Operations ==========

    /**
     * Delete workout session by ID
     */
    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteWorkoutSessionById(sessionId: String)

    /**
     * Delete workout sessions by user
     */
    @Query("DELETE FROM workout_sessions WHERE userId = :userId")
    suspend fun deleteUserWorkoutSessions(userId: String)

    /**
     * Delete abandoned workout sessions (older than specified time)
     */
    @Query("DELETE FROM workout_sessions WHERE status = 'ABANDONED' AND startTime < :cutoffTime")
    suspend fun deleteOldAbandonedSessions(cutoffTime: Long)

    /**
     * Delete all workout sessions (for testing/reset purposes)
     */
    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAllWorkoutSessions()

    // ========== Statistics and Analytics ==========

    /**
     * Get total workout time for user (excluding paused duration)
     */
    @Query("""
        SELECT SUM(endTime - startTime - pausedDuration) 
        FROM workout_sessions 
        WHERE userId = :userId AND status = 'COMPLETED' AND endTime IS NOT NULL
    """)
    suspend fun getTotalWorkoutTime(userId: String): Long?

    /**
     * Get average workout duration for user
     */
    @Query("""
        SELECT AVG(endTime - startTime - pausedDuration) 
        FROM workout_sessions 
        WHERE userId = :userId AND status = 'COMPLETED' AND endTime IS NOT NULL
    """)
    suspend fun getAverageWorkoutDuration(userId: String): Double?

    /**
     * Get total volume lifted by user
     */
    @Query("SELECT SUM(totalVolume) FROM workout_sessions WHERE userId = :userId AND status = 'COMPLETED'")
    suspend fun getTotalVolume(userId: String): Float?

    /**
     * Get workout frequency (sessions per week) for user in date range
     */
    @Query("""
        SELECT COUNT(*) 
        FROM workout_sessions 
        WHERE userId = :userId AND status = 'COMPLETED' 
        AND startTime BETWEEN :startDate AND :endDate
    """)
    suspend fun getWorkoutFrequency(userId: String, startDate: Long, endDate: Long): Int

    /**
     * Get average completion percentage for user
     */
    @Query("SELECT AVG(completionPercentage) FROM workout_sessions WHERE userId = :userId AND status = 'COMPLETED'")
    suspend fun getAverageCompletionPercentage(userId: String): Float?

    /**
     * Get workout sessions with high completion rate (90%+)
     */
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND completionPercentage >= 90.0 AND status = 'COMPLETED' ORDER BY startTime DESC")
    fun getHighCompletionSessions(userId: String): Flow<List<WorkoutSession>>

    /**
     * Get workout sessions by month count
     */
    @Query("""
        SELECT COUNT(*) as sessionCount
        FROM workout_sessions 
        WHERE userId = :userId AND status = 'COMPLETED'
        AND strftime('%Y-%m', datetime(startTime/1000, 'unixepoch')) = :monthYear
    """)
    suspend fun getMonthlySessionCount(userId: String, monthYear: String): Int

    /**
     * Get monthly average completion percentage
     */
    @Query("""
        SELECT AVG(completionPercentage) as avgCompletion
        FROM workout_sessions 
        WHERE userId = :userId AND status = 'COMPLETED'
        AND strftime('%Y-%m', datetime(startTime/1000, 'unixepoch')) = :monthYear
    """)
    suspend fun getMonthlyAverageCompletion(userId: String, monthYear: String): Float?

    /**
     * Get monthly total volume
     */
    @Query("""
        SELECT SUM(totalVolume) as totalVolume
        FROM workout_sessions 
        WHERE userId = :userId AND status = 'COMPLETED'
        AND strftime('%Y-%m', datetime(startTime/1000, 'unixepoch')) = :monthYear
    """)
    suspend fun getMonthlyTotalVolume(userId: String, monthYear: String): Float?

    /**
     * Get most used workout plan ID
     */
    @Query("""
        SELECT planId
        FROM workout_sessions 
        WHERE userId = :userId AND status = 'COMPLETED'
        GROUP BY planId 
        ORDER BY COUNT(*) DESC 
        LIMIT 1
    """)
    suspend fun getMostUsedWorkoutPlanId(userId: String): String?

    /**
     * Get most used workout plan name
     */
    @Query("""
        SELECT planName
        FROM workout_sessions 
        WHERE userId = :userId AND status = 'COMPLETED'
        GROUP BY planId 
        ORDER BY COUNT(*) DESC 
        LIMIT 1
    """)
    suspend fun getMostUsedWorkoutPlanName(userId: String): String?

    /**
     * Get usage count for most used plan
     */
    @Query("""
        SELECT COUNT(*) as usageCount
        FROM workout_sessions 
        WHERE userId = :userId AND status = 'COMPLETED'
        GROUP BY planId 
        ORDER BY usageCount DESC 
        LIMIT 1
    """)
    suspend fun getMostUsedPlanCount(userId: String): Int

    /**
     * Get current workout streak (consecutive days with completed workouts)
     * Simplified version for Room compatibility
     */
    @Query("""
        SELECT COUNT(DISTINCT date(startTime/1000, 'unixepoch')) as streak
        FROM workout_sessions 
        WHERE userId = :userId 
        AND status = 'COMPLETED'
        AND date(startTime/1000, 'unixepoch') >= date('now', '-30 days')
        ORDER BY startTime DESC
    """)
    suspend fun getCurrentWorkoutStreak(userId: String): Int

    /**
     * Get workout dates for streak calculation (helper method)
     * Returns list of workout dates in descending order
     */
    @Query("""
        SELECT DISTINCT date(startTime/1000, 'unixepoch') as workout_date
        FROM workout_sessions 
        WHERE userId = :userId AND status = 'COMPLETED'
        ORDER BY workout_date DESC
        LIMIT 30
    """)
    suspend fun getRecentWorkoutDates(userId: String): List<String>
}