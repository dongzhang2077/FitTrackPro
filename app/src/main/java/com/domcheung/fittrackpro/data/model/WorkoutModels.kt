package com.domcheung.fittrackpro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// ========== Core Entities ==========

/**
 * Exercise entity from Wger API
 * Stores all available exercises with detailed information
 */
@Entity(tableName = "exercises")
@TypeConverters(Converters::class)
data class Exercise(
    @PrimaryKey
    val id: Int,                    // Wger API ID
    val name: String,               // Exercise name
    val description: String = "",   // Exercise description
    val category: String = "",      // Muscle group category
    val muscles: List<String> = emptyList(),       // Primary muscles
    val secondaryMuscles: List<String> = emptyList(), // Secondary muscles
    val equipment: List<String> = emptyList(),     // Required equipment
    val imageUrl: String? = null,   // Exercise image URL
    val videoUrl: String? = null,   // Exercise video URL
    val instructions: String = "",  // Exercise instructions
    val createdAt: Long = System.currentTimeMillis(),
    val isCustom: Boolean = false,  // User-created custom exercise
    val syncedToFirebase: Boolean = false // Firebase sync status
)

/**
 * Workout Plan entity
 * Template for creating workout sessions
 */
@Entity(tableName = "workout_plans")
@TypeConverters(Converters::class)
data class WorkoutPlan(
    @PrimaryKey
    val id: String,                 // UUID
    val name: String,               // Plan name
    val description: String = "",   // Plan description
    val targetMuscleGroups: List<String> = emptyList(), // Target muscle groups
    val estimatedDuration: Int = 0, // Estimated duration in minutes
    val exercises: List<PlannedExercise> = emptyList(), // Exercises in plan
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",     // User ID
    val isTemplate: Boolean = true, // Is this a reusable template
    val tags: List<String> = emptyList(), // Tags for categorization
    val syncedToFirebase: Boolean = false // Firebase sync status
)

/**
 * Workout Session entity - actual workout execution
 * Tracks real-time workout progress and modifications
 */
@Entity(tableName = "workout_sessions")
@TypeConverters(Converters::class)
data class WorkoutSession(
    @PrimaryKey
    val id: String,                 // UUID
    val planId: String,             // Reference to WorkoutPlan
    val planName: String,           // Cached plan name
    val originalPlan: WorkoutPlan,  // Original plan (for comparison)
    val currentPlan: WorkoutPlan,   // Current plan (may be modified during workout)
    val userId: String,             // User ID
    val startTime: Long,            // Workout start timestamp
    val endTime: Long? = null,      // Workout end timestamp
    val pauseStartTime: Long? = null,
    val pausedDuration: Long = 0,   // Total paused time (milliseconds)
    val status: WorkoutStatus,      // Current workout status
    val exercises: List<ExecutedExercise> = emptyList(), // Executed exercises
    val totalVolume: Float = 0f,    // Total volume (weight × reps)
    val completionPercentage: Float = 0f, // Completion percentage
    val notes: String = "",         // Session notes
    val createdAt: Long = System.currentTimeMillis(),
    val syncedToFirebase: Boolean = false // Firebase sync status
)

/**
 * Personal Record entity
 * Tracks user's best achievements for each exercise
 */
@Entity(tableName = "personal_records")
@TypeConverters(Converters::class)
data class PersonalRecord(
    @PrimaryKey
    val id: String,                 // UUID
    val exerciseId: Int,            // Reference to Exercise
    val exerciseName: String,       // Cached exercise name
    val userId: String,             // User ID
    val recordType: RecordType,     // Type of record
    val weight: Float,              // Weight achieved
    val reps: Int,                  // Repetitions achieved
    val oneRepMax: Float,           // Calculated 1RM
    val volume: Float,              // Total volume (weight × reps)
    val achievedAt: Long,           // When record was achieved
    val sessionId: String,          // Reference to WorkoutSession
    val createdAt: Long = System.currentTimeMillis(),
    val syncedToFirebase: Boolean = false // Firebase sync status
)

// ========== Supporting Data Classes ==========

/**
 * Planned exercise within a workout plan
 * Defines the exercise configuration in a template
 */
data class PlannedExercise(
    val exerciseId: Int,            // Reference to Exercise
    val exerciseName: String,       // Cached exercise name
    val orderIndex: Int,            // Order in the plan
    val sets: List<PlannedSet>,     // Planned sets
    val restBetweenSets: Int = 90,  // Rest time between sets (seconds)
    val notes: String = ""          // Exercise-specific notes
)

/**
 * Planned set configuration
 * Defines target weight and reps for a set
 */
data class PlannedSet(
    val setNumber: Int,             // Set number (1, 2, 3...)
    val targetWeight: Float,        // Target weight (kg)
    val targetReps: Int,            // Target repetitions
    val targetRpe: Float? = null,   // Target RPE (Rate of Perceived Exertion)
    val restAfter: Int = 90         // Rest time after this set (seconds)
)

/**
 * Executed exercise during workout session
 * Tracks actual performance vs planned performance
 */
data class ExecutedExercise(
    val exerciseId: Int,            // Reference to Exercise
    val exerciseName: String,       // Cached exercise name
    val orderIndex: Int,            // Order in execution (may differ from plan)
    val plannedSets: List<PlannedSet>, // Original planned sets
    val executedSets: List<ExecutedSet>, // Actually executed sets
    val restBetweenSets: Int = 90,  // Rest time between sets
    val startTime: Long? = null,    // Exercise start time
    val endTime: Long? = null,      // Exercise end time
    val isCompleted: Boolean = false, // Is exercise fully completed
    val isReplaced: Boolean = false,  // Was this exercise replaced
    val replacedFromId: Int? = null,  // Original exercise ID if replaced
    val notes: String = ""          // Exercise notes
)

/**
 * Executed set during workout
 * Records actual performance for each set
 */
data class ExecutedSet(
    val setNumber: Int,             // Set number
    val plannedWeight: Float,       // Originally planned weight
    val plannedReps: Int,           // Originally planned reps
    val actualWeight: Float,        // Actually used weight
    val actualReps: Int,            // Actually completed reps
    val actualRpe: Float? = null,   // Actual RPE
    val restAfter: Int = 90,        // Rest time after this set
    val completedAt: Long? = null,  // Completion timestamp
    val isCompleted: Boolean = false, // Is set completed
    val isSkipped: Boolean = false, // Was set skipped
    val notes: String = ""          // Set-specific notes
)

// ========== Enums ==========

/**
 * Workout session status enumeration
 * Tracks the current state of a workout session
 */
enum class WorkoutStatus {
    NOT_STARTED,    // Workout not started yet
    IN_PROGRESS,    // Currently working out
    PAUSED,         // Workout paused
    RESTING,        // Between sets (resting)
    COMPLETED,      // Workout completed
    ABANDONED       // Workout abandoned without completion
}

/**
 * Personal record types
 * Different types of achievements to track
 */
enum class RecordType {
    MAX_WEIGHT,     // Maximum weight for any rep count
    MAX_REPS,       // Maximum reps for any weight
    MAX_VOLUME,     // Maximum volume in single set
    MAX_ONE_REP_MAX // Maximum calculated 1RM
}

// ========== Type Converters ==========

/**
 * Room type converters for complex data types
 * Converts between objects and JSON strings for database storage
 */
class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            Gson().fromJson<List<String>>(value, object : TypeToken<List<String>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromPlannedExerciseList(value: List<PlannedExercise>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toPlannedExerciseList(value: String): List<PlannedExercise> {
        return try {
            Gson().fromJson<List<PlannedExercise>>(value, object : TypeToken<List<PlannedExercise>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromExecutedExerciseList(value: List<ExecutedExercise>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toExecutedExerciseList(value: String): List<ExecutedExercise> {
        return try {
            Gson().fromJson<List<ExecutedExercise>>(value, object : TypeToken<List<ExecutedExercise>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromWorkoutPlan(value: WorkoutPlan): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toWorkoutPlan(value: String): WorkoutPlan {
        return try {
            Gson().fromJson(value, WorkoutPlan::class.java)
        } catch (e: Exception) {
            WorkoutPlan(id = "", name = "")
        }
    }

    @TypeConverter
    fun fromWorkoutStatus(value: WorkoutStatus): String {
        return value.name
    }

    @TypeConverter
    fun toWorkoutStatus(value: String): WorkoutStatus {
        return try {
            WorkoutStatus.valueOf(value)
        } catch (e: Exception) {
            WorkoutStatus.NOT_STARTED
        }
    }

    @TypeConverter
    fun fromRecordType(value: RecordType): String {
        return value.name
    }

    @TypeConverter
    fun toRecordType(value: String): RecordType {
        return try {
            RecordType.valueOf(value)
        } catch (e: Exception) {
            RecordType.MAX_WEIGHT
        }
    }
}