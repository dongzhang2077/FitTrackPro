package com.domcheung.fittrackpro.data.repository

import android.util.Log
import com.domcheung.fittrackpro.data.local.dao.ExerciseDao
import com.domcheung.fittrackpro.data.local.dao.PersonalRecordDao
import com.domcheung.fittrackpro.data.local.dao.WorkoutPlanDao
import com.domcheung.fittrackpro.data.local.dao.WorkoutSessionDao
import com.domcheung.fittrackpro.data.model.ExecutedExercise
import com.domcheung.fittrackpro.data.model.Exercise
import com.domcheung.fittrackpro.data.model.PersonalRecord
import com.domcheung.fittrackpro.data.model.PlannedExercise
import com.domcheung.fittrackpro.data.model.PlannedSet
import com.domcheung.fittrackpro.data.model.RecordType
import com.domcheung.fittrackpro.data.model.WorkoutPlan
import com.domcheung.fittrackpro.data.model.WorkoutSession
import com.domcheung.fittrackpro.data.model.WorkoutStatus
import com.domcheung.fittrackpro.data.remote.WgerApiService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of WorkoutRepository interface
 * Handles data operations with Room local database and Firebase Firestore
 * Provides offline-first approach with background sync
 */
@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val workoutPlanDao: WorkoutPlanDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val personalRecordDao: PersonalRecordDao,
    private val firestore: FirebaseFirestore,
    private val wgerApiService: WgerApiService
) : WorkoutRepository {

    // ========== Exercise Operations ==========

    override fun getAllExercises(): Flow<List<Exercise>> {
        return exerciseDao.getAllExercisesPrioritizeImages()
    }

    override suspend fun getExerciseById(exerciseId: Int): Exercise? {
        return exerciseDao.getExerciseById(exerciseId)
    }

    override fun searchExercises(query: String): Flow<List<Exercise>> {
        return exerciseDao.searchExercises(query)
    }

    override fun getExercisesByCategory(category: String): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByCategory(category)
    }

    override fun getExercisesByEquipment(equipment: String): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByEquipment(equipment)
    }

    override fun getCustomExercises(): Flow<List<Exercise>> {
        return exerciseDao.getCustomExercises()
    }

    override suspend fun createCustomExercise(exercise: Exercise): Result<Long> {
        return try {
            val customExercise = exercise.copy(
                isCustom = true,
                createdAt = System.currentTimeMillis(),
                syncedToFirebase = false
            )
            exerciseDao.insertExercise(customExercise)
            Result.success(customExercise.id.toLong())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateExercise(exercise: Exercise): Result<Unit> {
        return try {
            exerciseDao.updateExercise(exercise.copy(syncedToFirebase = false))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCustomExercise(exerciseId: Int): Result<Unit> {
        return try {
            exerciseDao.deleteCustomExercise(exerciseId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncExercisesFromApi(): Result<Unit> {
        return try {
            Log.d("SYNC_API", "========================================")
            Log.d("SYNC_API", "Starting exercise sync from Wger API...")
            Log.d("SYNC_API", "========================================")

            // Fetch all data from Wger API
            Log.d("SYNC_API", "Fetching exercises...")
            val exercisesResponse = wgerApiService.getAllExercises()
            Log.d("SYNC_API", "✅ Fetched ${exercisesResponse.count} total exercises (${exercisesResponse.results.size} in this batch)")

            // Fetch exercise images separately
            Log.d("SYNC_API", "Fetching exercise images...")
            val imagesResponse = wgerApiService.getAllExerciseImages()
            Log.d("SYNC_API", "✅ Fetched ${imagesResponse.count} total images (${imagesResponse.results.size} in this batch)")


            // Debug: Log first few image mappings
            Log.d("SYNC_API", "📸 First 5 images from API:")
            imagesResponse.results.take(5).forEach { img ->
                Log.d("SYNC_API", "  Image id=${img.id}, exerciseBase=${img.exerciseBase}, exerciseBaseId=${img.exerciseBaseId}, exerciseId=${img.exerciseId}, url=${img.imageUrl}, isMain=${img.isMain}")
                Log.d("SYNC_API", "  → Resolved exercise ID: ${img.resolveExerciseId()}")
            }

            // Create a map of exerciseId -> image URL (prefer main images)
            val imageMap = mutableMapOf<Int, String>()
            imagesResponse.results.forEach { imageDto ->
                val exerciseId = imageDto.resolveExerciseId()
                if (exerciseId != null && exerciseId != 0) {
                    val existingImage = imageMap[exerciseId]
                    // Only update if this is a main image or if we don't have an image yet
                    if (existingImage == null || imageDto.isMain) {
                        val fullUrl = when {
                            imageDto.imageUrl.startsWith("http://") || imageDto.imageUrl.startsWith("https://") -> imageDto.imageUrl
                            imageDto.imageUrl.startsWith("/") -> "https://wger.de${imageDto.imageUrl}"
                            else -> "https://wger.de/${imageDto.imageUrl}"
                        }
                        imageMap[exerciseId] = fullUrl
                    }
                }
            }


            Log.d("SYNC_API", "📸 Mapped images for ${imageMap.size} exercises")
            Log.d("SYNC_API", "📸 First 5 exercise IDs with images: ${imageMap.keys.take(5).joinToString()}")
            Log.d("SYNC_API", "📸 First 5 exercise IDs from API: ${exercisesResponse.results.take(5).map { it.id }.joinToString()}")

            var skippedCount = 0
            var validCount = 0
            val exercisesWithImages = mutableListOf<Pair<String, String>>() // name to URL
            val exercisesWithVideos = mutableListOf<Pair<String, String>>() // name to URL

            // Map DTOs to local Exercise entities
            val exercises = exercisesResponse.results.mapIndexedNotNull { index, dto ->
                // Extract English name from translations array
                val englishTranslation = dto.translations?.firstOrNull { it.languageId == 2 }
                val exerciseName = englishTranslation?.name?.trim()

                // Skip if no valid name found
                if (exerciseName.isNullOrBlank()) {
                    skippedCount++
                    if (skippedCount <= 10) {
                        Log.d("SYNC_API", "⚠️ Skipping exercise #$index: id=${dto.id}, no English translation")
                    }
                    return@mapIndexedNotNull null
                }

                validCount++
                if (validCount <= 5) {
                    Log.d("SYNC_API", "✅ Valid exercise #$validCount: id=${dto.id}, name=$exerciseName, category=${dto.category?.name}")
                }

                // Extract description from translations (remove HTML tags)
                val description = englishTranslation.description?.let { desc ->
                    desc.replace(Regex("<[^>]*>"), "") // Remove HTML tags
                        .replace(Regex("\\s+"), " ") // Normalize whitespace
                        .trim()
                        .ifBlank { "No description available" }
                } ?: "No description available"

                // Get image URL from the separate images map
                val imageUrl = imageMap[dto.id]
                // Get the first video URL directly from the exercise DTO, if available
                val videoUrl = dto.videos?.firstOrNull()?.videoUrl

                if (validCount <= 5) {
                    Log.d("SYNC_API", "  📸 Image URL: ${imageUrl ?: "none"}")
                    Log.d("SYNC_API", "  🎥 Video URL: ${videoUrl ?: "none"}")
                }

                // Track exercises with images for logging
                if (imageUrl != null && exercisesWithImages.size < 10) {
                    exercisesWithImages.add(exerciseName to imageUrl)
                }
                // Track exercises with videos for logging
                if (videoUrl != null && exercisesWithVideos.size < 10) {
                    exercisesWithVideos.add(exerciseName to videoUrl)
                }

                // Extract muscle names
                val primaryMuscles = dto.muscles?.map { it.nameEn ?: it.name } ?: emptyList()
                val secondaryMuscles = dto.musclesSecondary?.map { it.nameEn ?: it.name } ?: emptyList()

                // Extract equipment names
                val equipmentNames = dto.equipment?.map { it.name } ?: emptyList()

                Exercise(
                    id = dto.id,
                    name = exerciseName,
                    description = description,
                    category = dto.category?.name ?: "Uncategorized",
                    muscles = primaryMuscles,
                    secondaryMuscles = secondaryMuscles,
                    equipment = equipmentNames,
                    imageUrl = imageUrl,
                    videoUrl = videoUrl, // Videos can be added later
                    instructions = description,
                    isCustom = false,
                    createdAt = System.currentTimeMillis(),
                    syncedToFirebase = false
                )
            }

            Log.d("SYNC_API", "========================================")
            Log.d("SYNC_API", "API returned: ${exercisesResponse.results.size} exercises")
            Log.d("SYNC_API", "Skipped (no English translation): $skippedCount exercises")
            Log.d("SYNC_API", "Valid exercises: $validCount")
            Log.d("SYNC_API", "Exercises with images: ${exercises.count { it.imageUrl != null }}")
            Log.d("SYNC_API", "Mapped to entities: ${exercises.size} exercises")
            Log.d("SYNC_API", "========================================")

            // Log the first 10 exercises that have images
            if (exercisesWithImages.isNotEmpty()) {
                Log.d("SYNC_API", "🖼️ First 10 exercises WITH images (search for these!):")
                exercisesWithImages.forEachIndexed { index, (name, url) ->
                    Log.d("SYNC_API", "  ${index + 1}. \"$name\" -> $url")
                }
            } else {
                Log.d("SYNC_API", "⚠️ WARNING: No exercises have images! Image ID mapping may be broken.")
            }
            // Log the first 10 exercises that have videos
            if (exercisesWithVideos.isNotEmpty()) {
                Log.d("SYNC_API", "🎥 First 10 exercises WITH videos (search for these!):")
                exercisesWithVideos.forEachIndexed { index, (name, url) ->
                    Log.d("SYNC_API", "  ${index + 1}. \"$name\" -> $url")
                }
            } else {
                Log.d("SYNC_API", "⚠️ WARNING: No exercises have videos! Video ID mapping may be broken.")
            }
            Log.d("SYNC_API", "========================================")

            Log.d("SYNC_API", "Clearing old API exercises from database...")

            // Get count before clearing
            val countBefore = exerciseDao.getExerciseCount()
            Log.d("SYNC_API", "Database had $countBefore exercises before clearing")

            // Clear old API exercises (keeps custom exercises) and insert new ones
            exerciseDao.clearApiExercises()

            val countAfterClear = exerciseDao.getExerciseCount()
            Log.d("SYNC_API", "Database has $countAfterClear exercises after clearing (custom exercises)")

            Log.d("SYNC_API", "Inserting ${exercises.size} new exercises...")
            exerciseDao.insertExercises(exercises)

            val countAfterInsert = exerciseDao.getExerciseCount()
            Log.d("SYNC_API", "Database now has $countAfterInsert exercises total")

            Log.d("SYNC_API", "========================================")
            Log.d("SYNC_API", "✅ Successfully synced ${exercises.size} exercises to database")
            Log.d("SYNC_API", "========================================")

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("SYNC_API", "========================================")
            Log.e("SYNC_API", "❌ Error syncing from API")
            Log.e("SYNC_API", "Error type: ${e.javaClass.simpleName}")
            Log.e("SYNC_API", "Error message: ${e.message}")
            Log.e("SYNC_API", "========================================")
            e.printStackTrace()

            // Fallback: Create sample exercises if database is empty
            createSampleExercisesIfEmpty()

            Result.failure(e)
        }
    }

    private suspend fun createSampleExercisesIfEmpty() {
        println("SYNC_DEBUG: createSampleExercisesIfEmpty function called.")
        if (exerciseDao.getExerciseCount() == 0) {
            println("SYNC_DEBUG: DB is still empty, creating 3 sample exercises.")
            val sampleExercises = createSampleExercises()
            exerciseDao.insertExercises(sampleExercises)
            println("SYNC_DEBUG: Finished inserting sample exercises.")
        } else {
            println("SYNC_DEBUG: DB was not empty, skipping sample creation.")
        }
    }

    // ========== Workout Plan Operations ==========

    override fun getUserWorkoutPlans(userId: String): Flow<List<WorkoutPlan>> {
        return workoutPlanDao.getUserWorkoutPlans(userId)
    }

    override suspend fun getWorkoutPlanById(planId: String): WorkoutPlan? {
        return workoutPlanDao.getWorkoutPlanById(planId)
    }

    override fun getWorkoutPlanByIdFlow(planId: String): Flow<WorkoutPlan?> {
        return workoutPlanDao.getWorkoutPlanByIdFlow(planId)
    }

    override fun getWorkoutSessionByIdFlow(sessionId: String): Flow<WorkoutSession?> {
        return workoutSessionDao.getWorkoutSessionByIdFlow(sessionId)
    }

    override fun getTemplateWorkoutPlans(): Flow<List<WorkoutPlan>> {
        return workoutPlanDao.getTemplateWorkoutPlans()
    }

    override fun searchWorkoutPlans(userId: String, query: String): Flow<List<WorkoutPlan>> {
        return workoutPlanDao.searchWorkoutPlansAdvanced(query = query, userId = userId)
    }

    override suspend fun createWorkoutPlan(workoutPlan: WorkoutPlan): Result<String> {
        return try {
            val newPlan = workoutPlan.copy(
                id = if (workoutPlan.id.isEmpty()) UUID.randomUUID().toString() else workoutPlan.id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                syncedToFirebase = false
            )
            workoutPlanDao.insertWorkoutPlan(newPlan)
            Result.success(newPlan.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateWorkoutPlan(workoutPlan: WorkoutPlan): Result<Unit> {
        return try {
            val updatedPlan = workoutPlan.copy(
                updatedAt = System.currentTimeMillis(),
                syncedToFirebase = false
            )
            workoutPlanDao.updateWorkoutPlan(updatedPlan)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteWorkoutPlan(planId: String): Result<Unit> {
        return try {
            workoutPlanDao.deleteWorkoutPlanById(planId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun copyWorkoutPlan(planId: String, newName: String): Result<String> {
        return try {
            val originalPlan = workoutPlanDao.getWorkoutPlanById(planId)
                ?: return Result.failure(Exception("Plan not found"))

            val copiedPlan = originalPlan.copy(
                id = UUID.randomUUID().toString(),
                name = newName,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                syncedToFirebase = false
            )

            workoutPlanDao.insertWorkoutPlan(copiedPlan)
            Result.success(copiedPlan.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== Workout Session Operations ==========

    override fun getUserWorkoutSessions(userId: String): Flow<List<WorkoutSession>> {
        return workoutSessionDao.getUserWorkoutSessions(userId)
    }

    override suspend fun getWorkoutSessionById(sessionId: String): WorkoutSession? {
        return workoutSessionDao.getWorkoutSessionById(sessionId)
    }

    override suspend fun getActiveWorkoutSession(userId: String): WorkoutSession? {
        return workoutSessionDao.getActiveWorkoutSession(userId)
    }

    override fun getActiveWorkoutSessionFlow(userId: String): Flow<WorkoutSession?> {
        return workoutSessionDao.getActiveWorkoutSessionFlow(userId)
    }

    override fun getCompletedWorkoutSessions(userId: String): Flow<List<WorkoutSession>> {
        return workoutSessionDao.getCompletedWorkoutSessions(userId)
    }

    override fun getWorkoutSessionsByDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<WorkoutSession>> {
        return workoutSessionDao.getWorkoutSessionsByDateRange(userId, startDate, endDate)
    }

    override suspend fun startWorkoutSession(planId: String, userId: String): Result<WorkoutSession> {
        return try {
            // Check if there's already an active session
            val activeSession = workoutSessionDao.getActiveWorkoutSession(userId)
            if (activeSession != null) {
                return Result.failure(Exception("Another workout session is already active"))
            }

            // Get the workout plan
            val plan = workoutPlanDao.getWorkoutPlanById(planId)
                ?: return Result.failure(Exception("Workout plan not found"))

            // --- NEW LOGIC START ---
            // Convert PlannedExercises to ExecutedExercises
            val executedExercises = plan.exercises.map { plannedExercise ->
                ExecutedExercise(
                    exerciseId = plannedExercise.exerciseId,
                    exerciseName = plannedExercise.exerciseName,
                    orderIndex = plannedExercise.orderIndex,
                    plannedSets = plannedExercise.sets,
                    executedSets = emptyList(), // Initially empty
                    restBetweenSets = plannedExercise.restBetweenSets
                )
            }
            // --- NEW LOGIC END ---

            // Create new workout session with the converted exercises
            val session = WorkoutSession(
                id = UUID.randomUUID().toString(),
                planId = planId,
                planName = plan.name,
                originalPlan = plan,
                currentPlan = plan, // Initially the same as original
                userId = userId,
                startTime = System.currentTimeMillis(),
                status = WorkoutStatus.IN_PROGRESS,
                exercises = executedExercises, // Use the new list here
                syncedToFirebase = false
            )

            workoutSessionDao.insertWorkoutSession(session)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateWorkoutSession(workoutSession: WorkoutSession): Result<Unit> {
        return try {
            val updatedSession = workoutSession.copy(syncedToFirebase = false)
            workoutSessionDao.updateWorkoutSession(updatedSession)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pauseWorkoutSession(sessionId: String, isResting: Boolean): Result<Unit> {
        return try {
            val session = workoutSessionDao.getWorkoutSessionById(sessionId)
                ?: return Result.failure(Exception("Session not found to pause"))

            // Set status to RESTING or PAUSED based on the context
            val newStatus = if (isResting) WorkoutStatus.RESTING else WorkoutStatus.PAUSED

            val updatedSession = session.copy(
                status = newStatus,
                pauseStartTime = System.currentTimeMillis() // Record pause/rest start time
            )
            workoutSessionDao.updateWorkoutSession(updatedSession)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resumeWorkoutSession(sessionId: String): Result<Unit> {
        return try {
            val session = workoutSessionDao.getWorkoutSessionById(sessionId)
                ?: return Result.failure(Exception("Session not found to resume"))

            if (session.pauseStartTime != null) {
                // Calculate the duration of this pause and add it to the total paused duration
                val pauseDuration = System.currentTimeMillis() - session.pauseStartTime
                val totalPausedDuration = session.pausedDuration + pauseDuration

                val updatedSession = session.copy(
                    status = WorkoutStatus.IN_PROGRESS,
                    pausedDuration = totalPausedDuration,
                    pauseStartTime = null // Clear pause start time
                )
                workoutSessionDao.updateWorkoutSession(updatedSession)
            } else {
                // If resuming without a pause start time (e.g. from force close), just change status
                workoutSessionDao.updateWorkoutSessionStatus(sessionId, WorkoutStatus.IN_PROGRESS)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeWorkoutSession(sessionId: String): Result<Unit> {
        return try {
            workoutSessionDao.updateWorkoutSessionStatus(sessionId, WorkoutStatus.COMPLETED)
            workoutSessionDao.updateWorkoutSessionEndTime(sessionId, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun abandonWorkoutSession(sessionId: String): Result<Unit> {
        return try {
            workoutSessionDao.updateWorkoutSessionStatus(sessionId, WorkoutStatus.ABANDONED)
            workoutSessionDao.updateWorkoutSessionEndTime(sessionId, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== Personal Record Operations ==========

    override fun getUserPersonalRecords(userId: String): Flow<List<PersonalRecord>> {
        return personalRecordDao.getUserPersonalRecords(userId)
    }

    override fun getPersonalRecordsByExercise(
        userId: String,
        exerciseId: Int
    ): Flow<List<PersonalRecord>> {
        return personalRecordDao.getPersonalRecordsByExercise(userId, exerciseId)
    }

    override suspend fun getBestPersonalRecord(
        userId: String,
        exerciseId: Int,
        recordType: RecordType
    ): PersonalRecord? {
        return when (recordType) {
            RecordType.MAX_WEIGHT -> personalRecordDao.getBestWeightRecord(userId, exerciseId)
            RecordType.MAX_REPS -> personalRecordDao.getBestRepRecord(userId, exerciseId)
            RecordType.MAX_VOLUME -> personalRecordDao.getBestVolumeRecord(userId, exerciseId)
            RecordType.MAX_ONE_REP_MAX -> personalRecordDao.getBest1RMRecord(userId, exerciseId)
        }
    }

    override suspend fun checkAndCreatePersonalRecord(
        userId: String,
        exerciseId: Int,
        exerciseName: String,
        weight: Float,
        reps: Int,
        sessionId: String
    ): Result<List<PersonalRecord>> {
        println("PR_DEBUG: --- Starting PR Check for exerciseId: $exerciseId ---")
        println("PR_DEBUG: Values: weight=$weight, reps=$reps")
        return try {
            val volume = weight * reps
            val oneRepMax = calculateOneRepMax(weight, reps)

            val newRecords = mutableListOf<PersonalRecord>()

            // Check for MAX_WEIGHT record
            val isNewWeightRecord = personalRecordDao.isNewRecord(userId, exerciseId, "MAX_WEIGHT", weight, 0, 0f, 0f)
            println("PR_DEBUG: Is new MAX_WEIGHT record? $isNewWeightRecord")
            if (isNewWeightRecord) {
                newRecords.add(createPersonalRecord(userId, exerciseId, exerciseName, RecordType.MAX_WEIGHT, weight, reps, oneRepMax, volume, sessionId))
            }

            // Check for MAX_REPS record
            val isNewRepsRecord = personalRecordDao.isNewRecord(userId, exerciseId, "MAX_REPS", 0f, reps, 0f, 0f)
            println("PR_DEBUG: Is new MAX_REPS record? $isNewRepsRecord")
            if (isNewRepsRecord) {
                newRecords.add(createPersonalRecord(userId, exerciseId, exerciseName, RecordType.MAX_REPS, weight, reps, oneRepMax, volume, sessionId))
            }

            // Check for MAX_VOLUME record
            val isNewVolumeRecord = personalRecordDao.isNewRecord(userId, exerciseId, "MAX_VOLUME", 0f, 0, volume, 0f)
            println("PR_DEBUG: Is new MAX_VOLUME record? $isNewVolumeRecord")
            if (isNewVolumeRecord) {
                newRecords.add(createPersonalRecord(userId, exerciseId, exerciseName, RecordType.MAX_VOLUME, weight, reps, oneRepMax, volume, sessionId))
            }

            // Check for MAX_ONE_REP_MAX record
            val isNew1RMRecord = personalRecordDao.isNewRecord(userId, exerciseId, "MAX_ONE_REP_MAX", 0f, 0, 0f, oneRepMax)
            println("PR_DEBUG: Is new MAX_ONE_REP_MAX record? $isNew1RMRecord")
            if (isNew1RMRecord) {
                newRecords.add(createPersonalRecord(userId, exerciseId, exerciseName, RecordType.MAX_ONE_REP_MAX, weight, reps, oneRepMax, volume, sessionId))
            }

            if (newRecords.isNotEmpty()) {
                println("PR_DEBUG: Found ${newRecords.size} new records. Inserting into DB.")
                personalRecordDao.insertPersonalRecords(newRecords)
            } else {
                println("PR_DEBUG: No new records were set.")
            }
            println("PR_DEBUG: --- PR Check Finished ---")
            Result.success(newRecords)
        } catch (e: Exception) {
            println("PR_DEBUG: Error during PR check: ${e.message}")
            Result.failure(e)
        }
    }


    override fun getRecentPersonalRecords(userId: String): Flow<List<PersonalRecord>> {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24L * 60L * 60L * 1000L)
        return personalRecordDao.getRecentPersonalRecords(userId, thirtyDaysAgo)
    }

    // ========== Statistics and Analytics ==========

    override suspend fun getWorkoutStatistics(userId: String): Result<WorkoutStatistics> {
        return try {
            val totalWorkouts = workoutSessionDao.getUserCompletedWorkoutSessionCount(userId)
            val totalTime = workoutSessionDao.getTotalWorkoutTime(userId) ?: 0L
            val avgDuration = workoutSessionDao.getAverageWorkoutDuration(userId) ?: 0.0
            val totalVolume = workoutSessionDao.getTotalVolume(userId) ?: 0f
            val currentStreak = workoutSessionDao.getCurrentWorkoutStreak(userId)
            val avgCompletion = workoutSessionDao.getAverageCompletionPercentage(userId) ?: 0f
            val totalRecords = personalRecordDao.getUserPersonalRecordCount(userId)

            val statistics = WorkoutStatistics(
                totalWorkouts = totalWorkouts,
                totalWorkoutTime = totalTime,
                averageWorkoutDuration = avgDuration.toLong(),
                totalVolumeLifted = totalVolume,
                currentStreak = currentStreak,
                longestStreak = currentStreak, // Simplified for now
                averageCompletionRate = avgCompletion,
                favoriteExercises = emptyList(), // TODO: Implement
                totalPersonalRecords = totalRecords
            )

            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMonthlyWorkoutSummary(
        userId: String,
        year: Int,
        month: Int
    ): Result<MonthlyWorkoutSummary> {
        return try {
            val monthYear = String.format("%04d-%02d", year, month)
            val totalWorkouts = workoutSessionDao.getMonthlySessionCount(userId, monthYear)
            val avgCompletion = workoutSessionDao.getMonthlyAverageCompletion(userId, monthYear) ?: 0f
            val totalVolume = workoutSessionDao.getMonthlyTotalVolume(userId, monthYear) ?: 0f
            val mostUsedPlan = workoutSessionDao.getMostUsedWorkoutPlanName(userId)

            val summary = MonthlyWorkoutSummary(
                year = year,
                month = month,
                totalWorkouts = totalWorkouts,
                totalTime = 0L, // TODO: Calculate monthly total time
                totalVolume = totalVolume,
                averageCompletion = avgCompletion,
                personalRecordsCount = 0, // TODO: Calculate monthly PR count
                mostUsedPlan = mostUsedPlan,
                workoutDates = emptyList() // TODO: Get workout dates for month
            )

            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentWorkoutStreak(userId: String): Int {
        return workoutSessionDao.getCurrentWorkoutStreak(userId)
    }

    override suspend fun getTotalVolumeLifted(userId: String): Float {
        return workoutSessionDao.getTotalVolume(userId) ?: 0f
    }

    // ========== Sync Operations ==========

    override suspend fun syncToFirebase(userId: String): Result<Unit> {
        return try {
            // TODO: Implement Firebase sync
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncFromFirebase(userId: String): Result<Unit> {
        return try {
            // TODO: Implement Firebase sync
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun hasUnsyncedData(): Boolean {
        return try {
            val unsyncedPlans = workoutPlanDao.getUnsyncedWorkoutPlans().isNotEmpty()
            val unsyncedSessions = workoutSessionDao.getUnsyncedWorkoutSessions().isNotEmpty()
            val unsyncedRecords = personalRecordDao.getUnsyncedPersonalRecords().isNotEmpty()
            unsyncedPlans || unsyncedSessions || unsyncedRecords
        } catch (e: Exception) {
            false
        }
    }

    // ========== Helper Methods ==========

    private fun createPersonalRecord(
        userId: String,
        exerciseId: Int,
        exerciseName: String,
        recordType: RecordType,
        weight: Float,
        reps: Int,
        oneRepMax: Float,
        volume: Float,
        sessionId: String
    ): PersonalRecord {
        return PersonalRecord(
            id = UUID.randomUUID().toString(),
            exerciseId = exerciseId,
            exerciseName = exerciseName,
            userId = userId,
            recordType = recordType,
            weight = weight,
            reps = reps,
            oneRepMax = oneRepMax,
            volume = volume,
            achievedAt = System.currentTimeMillis(),
            sessionId = sessionId,
            syncedToFirebase = false
        )
    }

    private fun calculateOneRepMax(weight: Float, reps: Int): Float {
        // Using Epley formula: 1RM = weight * (1 + reps/30)
        return if (reps == 1) weight else weight * (1 + reps / 30f)
    }

    private fun createSampleExercises(): List<Exercise> {
        return listOf(
            Exercise(
                id = 1,
                name = "Bench Press",
                description = "Chest exercise performed lying on a bench",
                category = "Chest",
                muscles = listOf("Pectorals", "Triceps", "Deltoids"),
                equipment = listOf("Barbell", "Bench"),
                instructions = "Lie on bench, grip barbell, lower to chest, press up"
            ),
            Exercise(
                id = 2,
                name = "Squat",
                description = "Lower body exercise",
                category = "Legs",
                muscles = listOf("Quadriceps", "Glutes", "Hamstrings"),
                equipment = listOf("Barbell"),
                instructions = "Stand with barbell on shoulders, squat down, stand up"
            ),
            Exercise(
                id = 3,
                name = "Deadlift",
                description = "Full body pulling exercise",
                category = "Back",
                muscles = listOf("Hamstrings", "Glutes", "Erector Spinae"),
                equipment = listOf("Barbell"),
                instructions = "Lift barbell from ground to standing position"
            )
        )
    }


    /**
     * Seeds the database with initial sample workout plans for a user if they have none.
     * This ensures new users have content to interact with immediately.
     */
    override suspend fun seedInitialPlansIfEmpty(userId: String) {
        try {
            val planCount = workoutPlanDao.getUserWorkoutPlanCount(userId)
            if (planCount == 0) {
                // User has no plans, create sample ones for them
                val samplePlans = createSampleWorkoutPlans(userId)
                workoutPlanDao.insertWorkoutPlans(samplePlans)
            }
        } catch (e: Exception) {
            // Log error or handle it silently
            // It's not critical if seeding fails, user can create plans manually
            println("Error seeding initial workout plans: ${e.message}")
        }
    }

    /**
     * Creates a list of sample workout plans.
     */
    private fun createSampleWorkoutPlans(userId: String): List<WorkoutPlan> {
        val fullBodyPlan = WorkoutPlan(
            id = UUID.randomUUID().toString(),
            name = "Full Body Strength - Beginner",
            description = "A great starting point to build foundational strength.",
            targetMuscleGroups = listOf("Full Body", "Strength"),
            estimatedDuration = 45,
            exercises = listOf(
                PlannedExercise(
                    exerciseId = 2, // Squat
                    exerciseName = "Squat",
                    orderIndex = 0,
                    sets = listOf(
                        PlannedSet(setNumber = 1, targetWeight = 40f, targetReps = 8),
                        PlannedSet(setNumber = 2, targetWeight = 40f, targetReps = 8),
                        PlannedSet(setNumber = 3, targetWeight = 40f, targetReps = 8)
                    )
                ),
                PlannedExercise(
                    exerciseId = 1, // Bench Press
                    exerciseName = "Bench Press",
                    orderIndex = 1,
                    sets = listOf(
                        PlannedSet(setNumber = 1, targetWeight = 30f, targetReps = 8),
                        PlannedSet(setNumber = 2, targetWeight = 30f, targetReps = 8),
                        PlannedSet(setNumber = 3, targetWeight = 30f, targetReps = 8)
                    )
                ),
                PlannedExercise(
                    exerciseId = 3, // Deadlift
                    exerciseName = "Deadlift",
                    orderIndex = 2,
                    sets = listOf(
                        PlannedSet(setNumber = 1, targetWeight = 50f, targetReps = 5),
                        PlannedSet(setNumber = 2, targetWeight = 50f, targetReps = 5)
                    )
                )
            ),
            createdBy = userId,
            isTemplate = true,
            syncedToFirebase = false
        )
        return listOf(fullBodyPlan)
    }

    override suspend fun getExercisesByIds(ids: List<Int>): List<Exercise> {
        return try {
            exerciseDao.getExercisesByIds(ids)
        } catch (e: Exception) {
            // Return an empty list in case of an error
            emptyList()
        }
    }
}
