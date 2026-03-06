package com.domcheung.fittrackpro.data.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.domcheung.fittrackpro.MainActivity
import com.domcheung.fittrackpro.R
import java.time.LocalDate

class WorkoutReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val ignoreDayFilter = inputData.getBoolean(KEY_IGNORE_DAY_FILTER, false)
        val isTestNotification = inputData.getBoolean(KEY_IS_TEST_NOTIFICATION, false)
        val isSnoozeNotification = inputData.getBoolean(KEY_IS_SNOOZE_NOTIFICATION, false)
        val selectedDays = parseSelectedDays(inputData.getString(KEY_SELECTED_DAYS))
        val today = LocalDate.now().dayOfWeek.value

        if (!ignoreDayFilter && selectedDays.isNotEmpty() && today !in selectedDays) {
            return Result.success()
        }

        createNotificationChannelIfNeeded()
        return try {
            showReminderNotification(isTestNotification, isSnoozeNotification)
            Result.success()
        } catch (_: SecurityException) {
            Result.success()
        }
    }

    private fun showReminderNotification(
        isTestNotification: Boolean,
        isSnoozeNotification: Boolean
    ) {
        val launchIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = ACTION_OPEN_WORKOUT_FROM_REMINDER
            putExtra(MainActivity.EXTRA_OPEN_MAIN_TAB, MainActivity.MAIN_TAB_WORKOUT)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isTestNotification) {
            "Workout reminder test"
        } else if (isSnoozeNotification) {
            "Snoozed workout reminder"
        } else {
            "Workout reminder"
        }

        val body = if (isTestNotification) {
            "Test notification: tap to open Workout tab in FitTrack Pro."
        } else if (isSnoozeNotification) {
            "Your snooze just ended. Tap to jump into your workout."
        } else {
            "Time to move. Open FitTrack Pro and complete today's training."
        }

        val notificationId = if (isTestNotification) TEST_NOTIFICATION_ID else REMINDER_NOTIFICATION_ID
        val snoozeIntent = Intent(applicationContext, WorkoutReminderActionReceiver::class.java).apply {
            action = WorkoutReminderActionReceiver.ACTION_SNOOZE_REMINDER
            putExtra(
                WorkoutReminderActionReceiver.EXTRA_SNOOZE_MINUTES,
                WorkoutReminderScheduler.DEFAULT_SNOOZE_DELAY_MINUTES
            )
            putExtra(WorkoutReminderActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationId + 100,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_lock_idle_alarm,
                "Snooze 30m",
                snoozePendingIntent
            )
            .build()

        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun parseSelectedDays(value: String?): Set<Int> {
        if (value.isNullOrBlank()) {
            return emptySet()
        }

        return value.split(',')
            .mapNotNull { day -> day.trim().toIntOrNull()?.takeIf { it in 1..7 } }
            .toSet()
    }

    companion object {
        const val KEY_SELECTED_DAYS = "selected_days"
        const val KEY_IGNORE_DAY_FILTER = "ignore_day_filter"
        const val KEY_IS_TEST_NOTIFICATION = "is_test_notification"
        const val KEY_IS_SNOOZE_NOTIFICATION = "is_snooze_notification"
        const val UNIQUE_WORK_NAME = "workout_reminder_daily"
        const val TEST_WORK_NAME = "workout_reminder_test_once"
        const val SNOOZE_WORK_NAME = "workout_reminder_snooze_once"
        private const val CHANNEL_ID = "workout_reminder_channel"
        private const val CHANNEL_NAME = "Workout reminders"
        private const val CHANNEL_DESCRIPTION = "Reminders for scheduled workout days"
        const val REMINDER_NOTIFICATION_ID = 1201
        const val TEST_NOTIFICATION_ID = 1202
        private const val ACTION_OPEN_WORKOUT_FROM_REMINDER = "com.domcheung.fittrackpro.action.OPEN_WORKOUT_FROM_REMINDER"
    }
}
