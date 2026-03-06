package com.domcheung.fittrackpro.data.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val workManager: WorkManager by lazy { WorkManager.getInstance(context) }

    fun syncReminderSchedule(settings: ReminderSettings) {
        if (!settings.enabled) {
            cancelReminderSchedule()
            return
        }

        val initialDelayMillis = calculateInitialDelayMillis(settings.hour, settings.minute)
        val selectedDaysPayload = serializeDays(settings.selectedDays)

        val request = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    WorkoutReminderWorker.KEY_SELECTED_DAYS to selectedDaysPayload
                )
            )
            .addTag(WorkoutReminderWorker.UNIQUE_WORK_NAME)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WorkoutReminderWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    fun triggerTestReminder(selectedDays: Set<Int>) {
        val selectedDaysPayload = serializeDays(selectedDays)

        val request = OneTimeWorkRequestBuilder<WorkoutReminderWorker>()
            .setInputData(
                workDataOf(
                    WorkoutReminderWorker.KEY_SELECTED_DAYS to selectedDaysPayload,
                    WorkoutReminderWorker.KEY_IGNORE_DAY_FILTER to true,
                    WorkoutReminderWorker.KEY_IS_TEST_NOTIFICATION to true
                )
            )
            .addTag(WorkoutReminderWorker.TEST_WORK_NAME)
            .build()

        workManager.enqueueUniqueWork(
            WorkoutReminderWorker.TEST_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun triggerSnoozedReminder(delayMinutes: Long = DEFAULT_SNOOZE_DELAY_MINUTES) {
        val safeDelayMinutes = normalizeSnoozeDelayMinutes(delayMinutes)

        val request = OneTimeWorkRequestBuilder<WorkoutReminderWorker>()
            .setInitialDelay(safeDelayMinutes, TimeUnit.MINUTES)
            .setInputData(
                workDataOf(
                    WorkoutReminderWorker.KEY_IGNORE_DAY_FILTER to true,
                    WorkoutReminderWorker.KEY_IS_TEST_NOTIFICATION to false,
                    WorkoutReminderWorker.KEY_IS_SNOOZE_NOTIFICATION to true
                )
            )
            .addTag(WorkoutReminderWorker.SNOOZE_WORK_NAME)
            .build()

        workManager.enqueueUniqueWork(
            WorkoutReminderWorker.SNOOZE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelReminderSchedule() {
        workManager.cancelUniqueWork(WorkoutReminderWorker.UNIQUE_WORK_NAME)
    }

    internal fun calculateInitialDelayMillis(
        hour: Int,
        minute: Int,
        now: LocalDateTime = LocalDateTime.now()
    ): Long {
        val safeHour = hour.coerceIn(0, 23)
        val safeMinute = minute.coerceIn(0, 59)

        var nextRun = now
            .withHour(safeHour)
            .withMinute(safeMinute)
            .withSecond(0)
            .withNano(0)

        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }

        return Duration.between(now, nextRun).toMillis().coerceAtLeast(1000L)
    }

    private fun serializeDays(days: Set<Int>): String {
        val normalized = days.filter { day -> day in 1..7 }.ifEmpty { (1..7).toList() }
        return normalized.sorted().joinToString(separator = ",")
    }

    companion object {
        const val DEFAULT_SNOOZE_DELAY_MINUTES = 30L
        private const val MIN_SNOOZE_DELAY_MINUTES = 5L
        private const val MAX_SNOOZE_DELAY_MINUTES = 180L

        internal fun normalizeSnoozeDelayMinutes(delayMinutes: Long): Long {
            return delayMinutes.coerceIn(MIN_SNOOZE_DELAY_MINUTES, MAX_SNOOZE_DELAY_MINUTES)
        }
    }
}
