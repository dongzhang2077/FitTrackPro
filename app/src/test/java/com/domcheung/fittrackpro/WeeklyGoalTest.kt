package com.domcheung.fittrackpro

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.domcheung.fittrackpro.data.local.UserPreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class WeeklyGoalTest {

    private lateinit var mockContext: Context
    private lateinit var mockDataStore: DataStore<Preferences>
    private lateinit var userPreferencesManager: UserPreferencesManager
    private val WEEKLY_WORKOUT_GOAL = intPreferencesKey("weekly_workout_goal")

    @Before
    fun setup() {
        // Create mock objects
        mockContext = mock()
        mockDataStore = mock()
        
        // Create UserPreferencesManager with mocked context
        userPreferencesManager = UserPreferencesManager(mockContext)
    }

    @Test
    fun testSaveWeeklyGoal() = runBlocking {
        // Test saving a weekly goal
        val testGoal = 5
        
        // Create a mock Preferences object
        val mockPreferences = mock<Preferences>()
        whenever(mockPreferences[WEEKLY_WORKOUT_GOAL]).thenReturn(testGoal)
        
        // Mock the DataStore data flow
        whenever(mockDataStore.data).thenReturn(kotlinx.coroutines.flow.flowOf(mockPreferences))
        
        // Mock the DataStore edit operation
        whenever(mockDataStore.edit(any())).thenAnswer {
            val editAction = it.arguments[0] as (suspend (Preferences) -> Unit)
            runBlocking { editAction(mockPreferences) }
        }
        
        // Save the goal using UserPreferencesManager
        userPreferencesManager.saveWeeklyWorkoutGoal(testGoal)
        
        // Verify it was saved
        val savedGoal = userPreferencesManager.weeklyWorkoutGoal.first()
        assertEquals(testGoal, savedGoal)
    }

    @Test
    fun testGetWeeklyGoal() = runBlocking {
        // Test retrieving a weekly goal
        val testGoal = 4
        
        // Create a mock Preferences object
        val mockPreferences = mock<Preferences>()
        whenever(mockPreferences[WEEKLY_WORKOUT_GOAL]).thenReturn(testGoal)
        
        // Mock the DataStore data flow to return the test goal
        whenever(mockDataStore.data).thenReturn(kotlinx.coroutines.flow.flowOf(mockPreferences))
        
        // Retrieve the goal using UserPreferencesManager
        val retrievedGoal = userPreferencesManager.weeklyWorkoutGoal.first()
        assertEquals(testGoal, retrievedGoal)
    }

    @Test
    fun testDefaultWeeklyGoal() = runBlocking {
        // Test that the default weekly goal is 3 when none is set
        val mockPreferences = mock<Preferences>()
        whenever(mockPreferences[WEEKLY_WORKOUT_GOAL]).thenReturn(null)
        
        whenever(mockDataStore.data).thenReturn(kotlinx.coroutines.flow.flowOf(mockPreferences))
        
        val defaultGoal = userPreferencesManager.weeklyWorkoutGoal.first()
        assertEquals(3, defaultGoal)
    }

    @Test
    fun testWeeklyGoalRange() = runBlocking {
        // Test that weekly goals are within valid range (1-7)
        val validGoals = listOf(1, 2, 3, 4, 5, 6, 7)
        
        validGoals.forEach { goal ->
            // Create a mock Preferences object
            val mockPreferences = mock<Preferences>()
            whenever(mockPreferences[WEEKLY_WORKOUT_GOAL]).thenReturn(goal)
            
            // Mock the DataStore data flow
            whenever(mockDataStore.data).thenReturn(kotlinx.coroutines.flow.flowOf(mockPreferences))
            
            // Mock the DataStore edit operation
            whenever(mockDataStore.edit(any())).thenAnswer {
                val editAction = it.arguments[0] as (suspend (Preferences) -> Unit)
                runBlocking { editAction(mockPreferences) }
            }
            
            // Save the goal using UserPreferencesManager
            userPreferencesManager.saveWeeklyWorkoutGoal(goal)
            
            // Verify it was saved correctly
            val savedGoal = userPreferencesManager.weeklyWorkoutGoal.first()
            assertEquals(goal, savedGoal)
            assertTrue("Weekly goal should be between 1 and 7", savedGoal in 1..7)
        }
    }

    @Test
    fun testInvalidWeeklyGoal() = runBlocking {
        // Test that invalid goals outside the range (1-7) are still saved but should be handled by UI
        val invalidGoals = listOf(0, 8, 10, -1)
        
        invalidGoals.forEach { goal ->
            // Create a mock Preferences object
            val mockPreferences = mock<Preferences>()
            whenever(mockPreferences[WEEKLY_WORKOUT_GOAL]).thenReturn(goal)
            
            // Mock the DataStore data flow
            whenever(mockDataStore.data).thenReturn(kotlinx.coroutines.flow.flowOf(mockPreferences))
            
            // Mock the DataStore edit operation
            whenever(mockDataStore.edit(any())).thenAnswer {
                val editAction = it.arguments[0] as (suspend (Preferences) -> Unit)
                runBlocking { editAction(mockPreferences) }
            }
            
            // Save the goal using UserPreferencesManager
            userPreferencesManager.saveWeeklyWorkoutGoal(goal)
            
            // Verify it was saved (DataStore itself doesn't validate)
            val savedGoal = userPreferencesManager.weeklyWorkoutGoal.first()
            assertEquals(goal, savedGoal)
        }
    }
}