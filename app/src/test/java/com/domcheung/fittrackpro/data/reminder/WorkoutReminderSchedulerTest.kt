package com.domcheung.fittrackpro.data.reminder

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutReminderSchedulerTest {

    @Test
    fun normalizeSnoozeDelayMinutes_clampsBelowMinimum() {
        val normalized = WorkoutReminderScheduler.normalizeSnoozeDelayMinutes(1L)

        assertEquals(5L, normalized)
    }

    @Test
    fun normalizeSnoozeDelayMinutes_keepsInRangeValue() {
        val normalized = WorkoutReminderScheduler.normalizeSnoozeDelayMinutes(30L)

        assertEquals(30L, normalized)
    }

    @Test
    fun normalizeSnoozeDelayMinutes_clampsAboveMaximum() {
        val normalized = WorkoutReminderScheduler.normalizeSnoozeDelayMinutes(240L)

        assertEquals(180L, normalized)
    }
}
