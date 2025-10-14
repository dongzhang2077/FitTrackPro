package com.domcheung.fittrackpro.domain.usecase

import com.domcheung.fittrackpro.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for syncing user data to/from Firebase
 */
class SyncDataUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * Check if there is any unsynced data
     */
    suspend fun hasUnsyncedData(): Boolean {
        return try {
            workoutRepository.hasUnsyncedData()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Sync data to Firebase
     */
    suspend fun syncToFirebase(userId: String): Result<Unit> {
        return try {
            workoutRepository.syncToFirebase(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync data from Firebase
     */
    suspend fun syncFromFirebase(userId: String): Result<Unit> {
        return try {
            workoutRepository.syncFromFirebase(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Perform automatic background sync when conditions are met
     */
    suspend fun performAutoSync(userId: String): Result<Unit> {
        return try {
            if (hasUnsyncedData()) {
                // In a real implementation, this would sync actual data
                // For now, we just mark it as successful since Firebase sync is not implemented
                syncToFirebase(userId)
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}