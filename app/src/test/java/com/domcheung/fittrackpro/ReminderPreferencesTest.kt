package com.domcheung.fittrackpro

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.domcheung.fittrackpro.data.local.UserPreferencesManager
import com.domcheung.fittrackpro.data.reminder.ReminderSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class ReminderPreferencesTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var userPreferencesManager: UserPreferencesManager

    @Before
    fun setup() {
        val tempDir = Files.createTempDirectory("reminder_preferences_test_").toFile()
        tempDir.deleteOnExit()
        val dataStoreFile = File(tempDir, "user_preferences.preferences_pb")

        dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { dataStoreFile }
        )

        userPreferencesManager = UserPreferencesManager(dataStore)

        runBlocking {
            userPreferencesManager.clearUserData()
        }
    }

    @Test
    fun reminderDefaults_areCorrect() = runBlocking {
        assertFalse(userPreferencesManager.reminderEnabled.first())
        assertEquals(19, userPreferencesManager.reminderHour.first())
        assertEquals(0, userPreferencesManager.reminderMinute.first())
        assertEquals(setOf(1, 2, 3, 4, 5, 6, 7), userPreferencesManager.reminderSelectedDays.first())
    }

    @Test
    fun reminderSettings_persistAfterUpdates() = runBlocking {
        userPreferencesManager.updateReminderSettings(
            ReminderSettings(
                enabled = true,
                hour = 6,
                minute = 45,
                selectedDays = setOf(1, 3, 5)
            )
        )

        assertTrue(userPreferencesManager.reminderEnabled.first())
        assertEquals(6, userPreferencesManager.reminderHour.first())
        assertEquals(45, userPreferencesManager.reminderMinute.first())
        assertEquals(setOf(1, 3, 5), userPreferencesManager.reminderSelectedDays.first())
    }

    @Test
    fun updateReminderEnabled_updatesSingleFlag() = runBlocking {
        userPreferencesManager.updateReminderEnabled(true)

        assertTrue(userPreferencesManager.reminderEnabled.first())
    }

    @Test
    fun updateReminderSettings_savesWholeState() = runBlocking {
        val settings = ReminderSettings(
            enabled = true,
            hour = 8,
            minute = 30,
            selectedDays = setOf(2, 4, 6)
        )

        userPreferencesManager.updateReminderSettings(settings)

        assertEquals(settings, userPreferencesManager.reminderSettings.first())
    }
}
