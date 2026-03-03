package com.domcheung.fittrackpro

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.domcheung.fittrackpro.data.local.UserPreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.nio.file.Files

class WeeklyGoalTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var userPreferencesManager: UserPreferencesManager

    @Before
    fun setup() {
        val tempDir = Files.createTempDirectory("weekly_goal_test_").toFile()
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
    fun testSaveWeeklyGoal() = runBlocking {
        val testGoal = 5

        userPreferencesManager.saveWeeklyWorkoutGoal(testGoal)

        val savedGoal = userPreferencesManager.weeklyWorkoutGoal.first()
        assertEquals(testGoal, savedGoal)
    }

    @Test
    fun testGetWeeklyGoal() = runBlocking {
        val testGoal = 4

        userPreferencesManager.saveWeeklyWorkoutGoal(testGoal)

        val retrievedGoal = userPreferencesManager.weeklyWorkoutGoal.first()
        assertEquals(testGoal, retrievedGoal)
    }

    @Test
    fun testDefaultWeeklyGoal() = runBlocking {
        val defaultGoal = userPreferencesManager.weeklyWorkoutGoal.first()
        assertEquals(3, defaultGoal)
    }

    @Test
    fun testWeeklyGoalRange() = runBlocking {
        val goal = 6

        userPreferencesManager.saveWeeklyWorkoutGoal(goal)

        val savedGoal = userPreferencesManager.weeklyWorkoutGoal.first()
        assertEquals(goal, savedGoal)
        assertTrue(savedGoal in 1..7)
    }

    @Test
    fun testInvalidWeeklyGoal() = runBlocking {
        val goal = 0

        userPreferencesManager.saveWeeklyWorkoutGoal(goal)

        val savedGoal = userPreferencesManager.weeklyWorkoutGoal.first()
        assertEquals(goal, savedGoal)
    }
}
