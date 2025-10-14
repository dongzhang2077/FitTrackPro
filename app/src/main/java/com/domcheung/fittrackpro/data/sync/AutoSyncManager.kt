package com.domcheung.fittrackpro.data.sync

import android.util.Log
import com.domcheung.fittrackpro.domain.usecase.SyncDataUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for automatic data synchronization
 *
 * Handles automatic sync at appropriate time points:
 * - After completing workouts
 * - After creating/editing workout plans
 * - After creating personal records
 * - Periodic background sync
 *
 * Note: This is currently prepared for when Firebase sync is fully implemented.
 * The sync infrastructure is in place but the actual syncToFirebase() method
 * needs to be implemented in WorkoutRepositoryImpl.
 */
@Singleton
class AutoSyncManager @Inject constructor(
    private val syncDataUseCase: SyncDataUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isPeriodicSyncRunning = false

    companion object {
        private const val TAG = "AutoSyncManager"
        private const val PERIODIC_SYNC_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes

        // Feature flag - set to true once Firebase sync is fully implemented
        private const val AUTO_SYNC_ENABLED = false
    }

    /**
     * Trigger sync after completing a workout session
     */
    fun syncAfterWorkoutCompletion(userId: String) {
        if (!AUTO_SYNC_ENABLED) {
            Log.d(TAG, "Auto-sync disabled - skipping sync after workout completion")
            return
        }

        Log.d(TAG, "Triggering sync after workout completion")
        performSync(userId, "workout_completion")
    }

    /**
     * Trigger sync after creating or editing a workout plan
     */
    fun syncAfterPlanChange(userId: String) {
        if (!AUTO_SYNC_ENABLED) {
            Log.d(TAG, "Auto-sync disabled - skipping sync after plan change")
            return
        }

        Log.d(TAG, "Triggering sync after plan change")
        performSync(userId, "plan_change")
    }

    /**
     * Trigger sync after creating a personal record
     */
    fun syncAfterPersonalRecord(userId: String) {
        if (!AUTO_SYNC_ENABLED) {
            Log.d(TAG, "Auto-sync disabled - skipping sync after personal record")
            return
        }

        Log.d(TAG, "Triggering sync after personal record")
        performSync(userId, "personal_record")
    }

    /**
     * Start periodic background sync
     * Only syncs if there is unsynced data
     */
    fun startPeriodicSync(userId: String) {
        if (!AUTO_SYNC_ENABLED) {
            Log.d(TAG, "Auto-sync disabled - periodic sync not started")
            return
        }

        if (isPeriodicSyncRunning) {
            Log.d(TAG, "Periodic sync already running")
            return
        }

        isPeriodicSyncRunning = true
        Log.d(TAG, "Starting periodic sync")

        scope.launch {
            while (isPeriodicSyncRunning) {
                delay(PERIODIC_SYNC_INTERVAL_MS)

                try {
                    // Only sync if there's unsynced data
                    val hasUnsyncedData = syncDataUseCase.hasUnsyncedData()
                    if (hasUnsyncedData) {
                        Log.d(TAG, "Periodic sync: found unsynced data, starting sync")
                        performSync(userId, "periodic")
                    } else {
                        Log.d(TAG, "Periodic sync: no unsynced data, skipping")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Periodic sync check failed", e)
                }
            }
        }
    }

    /**
     * Stop periodic background sync
     */
    fun stopPeriodicSync() {
        Log.d(TAG, "Stopping periodic sync")
        isPeriodicSyncRunning = false
    }

    /**
     * Sync when app goes to background
     */
    fun syncOnAppBackground(userId: String) {
        if (!AUTO_SYNC_ENABLED) {
            Log.d(TAG, "Auto-sync disabled - skipping background sync")
            return
        }

        Log.d(TAG, "Triggering sync on app background")
        performSync(userId, "app_background")
    }

    /**
     * Perform the actual sync operation
     */
    private fun performSync(userId: String, trigger: String) {
        scope.launch {
            try {
                Log.d(TAG, "Starting sync (trigger: $trigger)")

                // Check if there's unsynced data first
                val hasUnsyncedData = syncDataUseCase.hasUnsyncedData()
                if (!hasUnsyncedData) {
                    Log.d(TAG, "No unsynced data found (trigger: $trigger)")
                    return@launch
                }

                // Perform auto sync
                val result = syncDataUseCase.performAutoSync(userId)

                result.fold(
                    onSuccess = {
                        Log.d(TAG, "Sync completed successfully (trigger: $trigger)")
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Sync failed (trigger: $trigger)", exception)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Sync error (trigger: $trigger)", e)
            }
        }
    }

    /**
     * Check if auto-sync is enabled
     */
    fun isAutoSyncEnabled(): Boolean = AUTO_SYNC_ENABLED
}
