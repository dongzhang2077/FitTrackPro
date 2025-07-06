package com.domcheung.fittrackpro.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.domcheung.fittrackpro.data.model.Exercise
import com.domcheung.fittrackpro.data.model.WorkoutPlan
import com.domcheung.fittrackpro.data.model.WorkoutSession
import com.domcheung.fittrackpro.data.model.PersonalRecord
import com.domcheung.fittrackpro.data.model.Converters
import com.domcheung.fittrackpro.data.local.dao.ExerciseDao
import com.domcheung.fittrackpro.data.local.dao.WorkoutPlanDao
import com.domcheung.fittrackpro.data.local.dao.WorkoutSessionDao
import com.domcheung.fittrackpro.data.local.dao.PersonalRecordDao

/**
 * FitTrack Pro Room Database
 * Main database configuration with all workout-related entities
 * Uses KSP for annotation processing and includes Firebase sync support
 */
@Database(
    entities = [
        Exercise::class,
        WorkoutPlan::class,
        WorkoutSession::class,
        PersonalRecord::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // ========== DAO Providers ==========

    /**
     * Provides access to Exercise data operations
     */
    abstract fun exerciseDao(): ExerciseDao

    /**
     * Provides access to WorkoutPlan data operations
     */
    abstract fun workoutPlanDao(): WorkoutPlanDao

    /**
     * Provides access to WorkoutSession data operations
     */
    abstract fun workoutSessionDao(): WorkoutSessionDao

    /**
     * Provides access to PersonalRecord data operations
     */
    abstract fun personalRecordDao(): PersonalRecordDao

    companion object {
        /**
         * Database name constant
         */
        const val DATABASE_NAME = "fittrack_pro_database"
    }
}