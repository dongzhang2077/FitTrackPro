package com.domcheung.fittrackpro.data.reminder

data class ReminderSettings(
    val enabled: Boolean = false,
    val hour: Int = 19,
    val minute: Int = 0,
    val selectedDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7)
)
