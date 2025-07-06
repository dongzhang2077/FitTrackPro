package com.domcheung.fittrackpro.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.domcheung.fittrackpro.data.model.PersonalRecord
import com.domcheung.fittrackpro.data.model.RecordType

/**
 * Data Access Object for PersonalRecord entity
 * Handles all personal record-related database operations including achievement tracking
 */
@Dao
interface PersonalRecordDao {

    // ========== Query Operations ==========

    /**
     * Get all personal records for user
     */
    @Query("SELECT * FROM personal_records WHERE userId = :userId ORDER BY achievedAt DESC")
    fun getUserPersonalRecords(userId: String): Flow<List<PersonalRecord>>

    /**
     * Get personal record by ID
     */
    @Query("SELECT * FROM personal_records WHERE id = :recordId")
    suspend fun getPersonalRecordById(recordId: String): PersonalRecord?

    /**
     * Get personal records for specific exercise
     */
    @Query("SELECT * FROM personal_records WHERE userId = :userId AND exerciseId = :exerciseId ORDER BY achievedAt DESC")
    fun getPersonalRecordsByExercise(userId: String, exerciseId: Int): Flow<List<PersonalRecord>>

    /**
     * Get personal records by type
     */
    @Query("SELECT * FROM personal_records WHERE userId = :userId AND recordType = :recordType ORDER BY achievedAt DESC")
    fun getPersonalRecordsByType(userId: String, recordType: RecordType): Flow<List<PersonalRecord>>

    /**
     * Get latest personal record for exercise by type
     */
    @Query("""
        SELECT * FROM personal_records 
        WHERE userId = :userId AND exerciseId = :exerciseId AND recordType = :recordType 
        ORDER BY achievedAt DESC LIMIT 1
    """)
    suspend fun getLatestPersonalRecord(userId: String, exerciseId: Int, recordType: RecordType): PersonalRecord?

    /**
     * Get best personal record for exercise (highest weight for MAX_WEIGHT type)
     */
    @Query("""
        SELECT * FROM personal_records 
        WHERE userId = :userId AND exerciseId = :exerciseId AND recordType = 'MAX_WEIGHT'
        ORDER BY weight DESC LIMIT 1
    """)
    suspend fun getBestWeightRecord(userId: String, exerciseId: Int): PersonalRecord?

    /**
     * Get best rep record for exercise (highest reps for MAX_REPS type)
     */
    @Query("""
        SELECT * FROM personal_records 
        WHERE userId = :userId AND exerciseId = :exerciseId AND recordType = 'MAX_REPS'
        ORDER BY reps DESC LIMIT 1
    """)
    suspend fun getBestRepRecord(userId: String, exerciseId: Int): PersonalRecord?

    /**
     * Get best volume record for exercise
     */
    @Query("""
        SELECT * FROM personal_records 
        WHERE userId = :userId AND exerciseId = :exerciseId AND recordType = 'MAX_VOLUME'
        ORDER BY volume DESC LIMIT 1
    """)
    suspend fun getBestVolumeRecord(userId: String, exerciseId: Int): PersonalRecord?

    /**
     * Get best 1RM record for exercise
     */
    @Query("""
        SELECT * FROM personal_records 
        WHERE userId = :userId AND exerciseId = :exerciseId AND recordType = 'MAX_ONE_REP_MAX'
        ORDER BY oneRepMax DESC LIMIT 1
    """)
    suspend fun getBest1RMRecord(userId: String, exerciseId: Int): PersonalRecord?

    /**
     * Get recent personal records (last 30 days)
     */
    @Query("""
        SELECT * FROM personal_records 
        WHERE userId = :userId AND achievedAt > :cutoffTime 
        ORDER BY achievedAt DESC
    """)
    fun getRecentPersonalRecords(userId: String, cutoffTime: Long): Flow<List<PersonalRecord>>

    /**
     * Get top personal records across all exercises (by weight)
     */
    @Query("""
        SELECT * FROM personal_records 
        WHERE userId = :userId AND recordType = 'MAX_WEIGHT'
        ORDER BY weight DESC LIMIT :limit
    """)
    fun getTopWeightRecords(userId: String, limit: Int = 10): Flow<List<PersonalRecord>>

    /**
     * Get personal records by date range
     */
    @Query("""
        SELECT * FROM personal_records 
        WHERE userId = :userId AND achievedAt BETWEEN :startDate AND :endDate 
        ORDER BY achievedAt DESC
    """)
    fun getPersonalRecordsByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<PersonalRecord>>

    /**
     * Get personal records not synced to Firebase
     */
    @Query("SELECT * FROM personal_records WHERE syncedToFirebase = 0")
    suspend fun getUnsyncedPersonalRecords(): List<PersonalRecord>

    /**
     * Check if personal record exists for exercise and type
     */
    @Query("""
        SELECT EXISTS(SELECT 1 FROM personal_records 
        WHERE userId = :userId AND exerciseId = :exerciseId AND recordType = :recordType)
    """)
    suspend fun hasPersonalRecord(userId: String, exerciseId: Int, recordType: RecordType): Boolean

    /**
     * Get total personal record count for user
     */
    @Query("SELECT COUNT(*) FROM personal_records WHERE userId = :userId")
    suspend fun getUserPersonalRecordCount(userId: String): Int

    /**
     * Get personal record count by exercise
     */
    @Query("SELECT COUNT(*) FROM personal_records WHERE userId = :userId AND exerciseId = :exerciseId")
    suspend fun getPersonalRecordCountByExercise(userId: String, exerciseId: Int): Int

    // ========== Insert Operations ==========

    /**
     * Insert single personal record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonalRecord(personalRecord: PersonalRecord)

    /**
     * Insert multiple personal records
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonalRecords(personalRecords: List<PersonalRecord>)

    // ========== Update Operations ==========

    /**
     * Update existing personal record
     */
    @Update
    suspend fun updatePersonalRecord(personalRecord: PersonalRecord)

    /**
     * Mark personal record as synced to Firebase
     */
    @Query("UPDATE personal_records SET syncedToFirebase = 1 WHERE id = :recordId")
    suspend fun markAsSynced(recordId: String)

    /**
     * Mark multiple personal records as synced
     */
    @Query("UPDATE personal_records SET syncedToFirebase = 1 WHERE id IN (:recordIds)")
    suspend fun markMultipleAsSynced(recordIds: List<String>)

    // ========== Delete Operations ==========

    /**
     * Delete personal record by ID
     */
    @Query("DELETE FROM personal_records WHERE id = :recordId")
    suspend fun deletePersonalRecordById(recordId: String)

    /**
     * Delete personal records for user
     */
    @Query("DELETE FROM personal_records WHERE userId = :userId")
    suspend fun deleteUserPersonalRecords(userId: String)

    /**
     * Delete personal records for specific exercise
     */
    @Query("DELETE FROM personal_records WHERE userId = :userId AND exerciseId = :exerciseId")
    suspend fun deletePersonalRecordsByExercise(userId: String, exerciseId: Int)

    /**
     * Delete old personal records (older than specified time)
     */
    @Query("DELETE FROM personal_records WHERE achievedAt < :cutoffTime")
    suspend fun deleteOldPersonalRecords(cutoffTime: Long)

    /**
     * Delete all personal records (for testing/reset purposes)
     */
    @Query("DELETE FROM personal_records")
    suspend fun deleteAllPersonalRecords()

    // ========== Statistics and Analytics ==========

    /**
     * Get total weight lifted across all exercises for user
     */
    @Query("SELECT SUM(weight * reps) FROM personal_records WHERE userId = :userId")
    suspend fun getTotalWeightLifted(userId: String): Float?

    /**
     * Get average 1RM across all exercises
     */
    @Query("SELECT AVG(oneRepMax) FROM personal_records WHERE userId = :userId AND recordType = 'MAX_ONE_REP_MAX'")
    suspend fun getAverage1RM(userId: String): Float?

    /**
     * Get personal record progression for exercise (weight over time)
     */
    @Query("""
        SELECT * FROM personal_records 
        WHERE userId = :userId AND exerciseId = :exerciseId AND recordType = 'MAX_WEIGHT'
        ORDER BY achievedAt ASC
    """)
    suspend fun getWeightProgression(userId: String, exerciseId: Int): List<PersonalRecord>

    /**
     * Get personal record count by month for user
     */
    @Query("""
        SELECT COUNT(*) as count
        FROM personal_records 
        WHERE userId = :userId 
        GROUP BY strftime('%Y-%m', datetime(achievedAt/1000, 'unixepoch'))
        ORDER BY strftime('%Y-%m', datetime(achievedAt/1000, 'unixepoch')) DESC
        LIMIT 12
    """)
    suspend fun getPersonalRecordCountsByMonth(userId: String): List<Int>

    /**
     * Get strongest exercises by 1RM (simplified version)
     */
    @Query("""
        SELECT * FROM personal_records 
        WHERE userId = :userId AND recordType = 'MAX_ONE_REP_MAX'
        GROUP BY exerciseId 
        HAVING oneRepMax = MAX(oneRepMax)
        ORDER BY oneRepMax DESC 
        LIMIT :limit
    """)
    suspend fun getStrongestExercises(userId: String, limit: Int = 5): List<PersonalRecord>

    /**
     * Check if new record is better than existing record
     */
    @Query("""
        SELECT CASE 
            WHEN :recordType = 'MAX_WEIGHT' THEN :weight > COALESCE(MAX(weight), 0)
            WHEN :recordType = 'MAX_REPS' THEN :reps > COALESCE(MAX(reps), 0)
            WHEN :recordType = 'MAX_VOLUME' THEN :volume > COALESCE(MAX(volume), 0)
            WHEN :recordType = 'MAX_ONE_REP_MAX' THEN :oneRepMax > COALESCE(MAX(oneRepMax), 0)
            ELSE 0
        END
        FROM personal_records 
        WHERE userId = :userId AND exerciseId = :exerciseId AND recordType = :recordType
    """)
    suspend fun isNewRecord(
        userId: String,
        exerciseId: Int,
        recordType: String,
        weight: Float,
        reps: Int,
        volume: Float,
        oneRepMax: Float
    ): Boolean

    /**
     * Get exercise ID with most personal records
     */
    @Query("""
        SELECT exerciseId
        FROM personal_records 
        WHERE userId = :userId 
        GROUP BY exerciseId 
        ORDER BY COUNT(*) DESC 
        LIMIT 1
    """)
    suspend fun getMostRecordedExerciseId(userId: String): Int?

    /**
     * Get exercise name with most personal records
     */
    @Query("""
        SELECT exerciseName
        FROM personal_records 
        WHERE userId = :userId 
        GROUP BY exerciseId 
        ORDER BY COUNT(*) DESC 
        LIMIT 1
    """)
    suspend fun getMostRecordedExerciseName(userId: String): String?

    /**
     * Get record count for most recorded exercise
     */
    @Query("""
        SELECT COUNT(*) as recordCount
        FROM personal_records 
        WHERE userId = :userId 
        GROUP BY exerciseId 
        ORDER BY recordCount DESC 
        LIMIT 1
    """)
    suspend fun getMostRecordedExerciseCount(userId: String): Int

    /**
     * Get personal records achieved this week
     */
    @Query("""
        SELECT * FROM personal_records 
        WHERE userId = :userId 
        AND achievedAt >= :weekStart
        ORDER BY achievedAt DESC
    """)
    fun getThisWeeksRecords(userId: String, weekStart: Long): Flow<List<PersonalRecord>>

    /**
     * Get personal records by session
     */
    @Query("SELECT * FROM personal_records WHERE sessionId = :sessionId ORDER BY achievedAt ASC")
    suspend fun getPersonalRecordsBySession(sessionId: String): List<PersonalRecord>

    /**
     * Get improvement rate for exercise (records per month)
     */
    @Query("""
        SELECT COUNT(*) as recordsPerMonth
        FROM personal_records 
        WHERE userId = :userId AND exerciseId = :exerciseId
        AND achievedAt >= :startDate
        GROUP BY strftime('%Y-%m', datetime(achievedAt/1000, 'unixepoch'))
        ORDER BY strftime('%Y-%m', datetime(achievedAt/1000, 'unixepoch')) DESC
    """)
    suspend fun getImprovementRate(userId: String, exerciseId: Int, startDate: Long): List<Int>

    /**
     * Get all-time best records across all exercises
     */
    @Query("""
        SELECT * FROM personal_records 
        WHERE userId = :userId 
        AND (
            (recordType = 'MAX_WEIGHT' AND weight = (SELECT MAX(weight) FROM personal_records WHERE userId = :userId AND recordType = 'MAX_WEIGHT')) OR
            (recordType = 'MAX_REPS' AND reps = (SELECT MAX(reps) FROM personal_records WHERE userId = :userId AND recordType = 'MAX_REPS')) OR
            (recordType = 'MAX_VOLUME' AND volume = (SELECT MAX(volume) FROM personal_records WHERE userId = :userId AND recordType = 'MAX_VOLUME')) OR
            (recordType = 'MAX_ONE_REP_MAX' AND oneRepMax = (SELECT MAX(oneRepMax) FROM personal_records WHERE userId = :userId AND recordType = 'MAX_ONE_REP_MAX'))
        )
        ORDER BY achievedAt DESC
    """)
    suspend fun getAllTimeBestRecords(userId: String): List<PersonalRecord>
}