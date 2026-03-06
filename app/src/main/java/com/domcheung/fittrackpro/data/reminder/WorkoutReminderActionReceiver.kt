package com.domcheung.fittrackpro.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class WorkoutReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SNOOZE_REMINDER) {
            return
        }

        val snoozeMinutes = intent
            .getLongExtra(EXTRA_SNOOZE_MINUTES, WorkoutReminderScheduler.DEFAULT_SNOOZE_DELAY_MINUTES)

        WorkoutReminderScheduler(context.applicationContext)
            .triggerSnoozedReminder(snoozeMinutes)

        val notificationId = intent.getIntExtra(
            EXTRA_NOTIFICATION_ID,
            WorkoutReminderWorker.REMINDER_NOTIFICATION_ID
        )
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    companion object {
        const val ACTION_SNOOZE_REMINDER = "com.domcheung.fittrackpro.action.SNOOZE_REMINDER"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
