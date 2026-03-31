package com.domcheung.fittrackpro

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.domcheung.fittrackpro.data.local.AppThemeMode
import com.domcheung.fittrackpro.data.local.AppThemeSettings
import com.domcheung.fittrackpro.data.local.UserPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class FitnessPreferencesTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var userPreferencesManager: UserPreferencesManager

    @Before
    fun setup() {
        val tempDir = Files.createTempDirectory("fitness_preferences_test_").toFile()
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
    fun defaultFitnessPreferencesAreReturnedWhenNotConfigured() = runBlocking {
        assertEquals("GENERAL_FITNESS", userPreferencesManager.fitnessPrimaryGoal.first())
        assertEquals(3, userPreferencesManager.fitnessWorkoutFrequency.first())
        assertEquals("BEGINNER", userPreferencesManager.fitnessExperienceLevel.first())
    }

    @Test
    fun saveFitnessPreferencesPersistsGoalFrequencyAndExperience() = runBlocking {
        userPreferencesManager.saveFitnessPreferences(
            primaryGoal = "MUSCLE_GAIN",
            workoutFrequencyPerWeek = 5,
            experienceLevel = "INTERMEDIATE"
        )

        assertEquals("MUSCLE_GAIN", userPreferencesManager.fitnessPrimaryGoal.first())
        assertEquals(5, userPreferencesManager.fitnessWorkoutFrequency.first())
        assertEquals("INTERMEDIATE", userPreferencesManager.fitnessExperienceLevel.first())
    }

    @Test
    fun saveFitnessPreferencesClampsFrequencyAndDefaultsBlankValues() = runBlocking {
        userPreferencesManager.saveFitnessPreferences(
            primaryGoal = "",
            workoutFrequencyPerWeek = 10,
            experienceLevel = ""
        )

        assertEquals("GENERAL_FITNESS", userPreferencesManager.fitnessPrimaryGoal.first())
        assertEquals(7, userPreferencesManager.fitnessWorkoutFrequency.first())
        assertEquals("BEGINNER", userPreferencesManager.fitnessExperienceLevel.first())
    }

    @Test
    fun defaultThemeSettingsAreSystemWithDynamicColorDisabled() = runBlocking {
        assertEquals(AppThemeMode.SYSTEM, userPreferencesManager.themeMode.first())
        assertEquals(false, userPreferencesManager.dynamicColorEnabled.first())
    }

    @Test
    fun updateThemeModePersistsSelectedMode() = runBlocking {
        userPreferencesManager.updateThemeMode(AppThemeMode.DARK)

        assertEquals(AppThemeMode.DARK, userPreferencesManager.themeMode.first())
    }

    @Test
    fun updateDynamicColorEnabledPersistsFlag() = runBlocking {
        userPreferencesManager.updateDynamicColorEnabled(true)

        assertEquals(true, userPreferencesManager.dynamicColorEnabled.first())
    }

    @Test
    fun updateThemeSettingsPersistsModeAndDynamicColorTogether() = runBlocking {
        userPreferencesManager.updateThemeSettings(
            AppThemeSettings(
                mode = AppThemeMode.LIGHT,
                dynamicColorEnabled = true
            )
        )

        val settings = userPreferencesManager.themeSettings.first()
        assertEquals(AppThemeMode.LIGHT, settings.mode)
        assertEquals(true, settings.dynamicColorEnabled)
    }
}
