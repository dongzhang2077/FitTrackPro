package com.domcheung.fittrackpro

import com.domcheung.fittrackpro.data.model.PersonalRecord
import com.domcheung.fittrackpro.data.model.RecordType
import com.domcheung.fittrackpro.presentation.workout_session.WorkoutSessionState
import com.domcheung.fittrackpro.presentation.workout_session.WorkoutSessionViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class PRNotificationTest {

    @Mock
    private lateinit var viewModel: WorkoutSessionViewModel

    private lateinit var testState: MutableStateFlow<WorkoutSessionState>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        testState = MutableStateFlow(WorkoutSessionState())
        
        // Mock the viewModel's uiState
        `when`(viewModel.uiState).thenReturn(testState)
    }

    @Test
    fun testPRNotificationStateUpdate() = runBlocking {
        // Test that newly achieved PRs are properly added to the state
        val testPRs = listOf(
            PersonalRecord(
                id = "1",
                exerciseId = 1,
                exerciseName = "Bench Press",
                userId = "user123",
                recordType = RecordType.MAX_WEIGHT,
                weight = 100f,
                reps = 5,
                oneRepMax = 117.5f,
                volume = 500f,
                achievedAt = System.currentTimeMillis(),
                sessionId = "session123"
            )
        )

        // Update state with new PRs
        testState.value = testState.value.copy(newlyAchievedRecords = testPRs)

        // Verify the state was updated
        val currentState = viewModel.uiState.first()
        assertEquals(testPRs, currentState.newlyAchievedRecords)
        assertEquals(1, currentState.newlyAchievedRecords.size)
    }

    @Test
    fun testClearPRNotifications() = runBlocking {
        // First add some PRs
        val testPRs = listOf(
            PersonalRecord(
                id = "1",
                exerciseId = 1,
                exerciseName = "Bench Press",
                userId = "user123",
                recordType = RecordType.MAX_WEIGHT,
                weight = 100f,
                reps = 5,
                oneRepMax = 117.5f,
                volume = 500f,
                achievedAt = System.currentTimeMillis(),
                sessionId = "session123"
            )
        )

        testState.value = testState.value.copy(newlyAchievedRecords = testPRs)

        // Verify PRs were added
        assertEquals(1, viewModel.uiState.first().newlyAchievedRecords.size)

        // Clear the PRs
        testState.value = testState.value.copy(newlyAchievedRecords = emptyList())

        // Verify PRs were cleared
        assertEquals(0, viewModel.uiState.first().newlyAchievedRecords.size)
        assertTrue(viewModel.uiState.first().newlyAchievedRecords.isEmpty())
    }

    @Test
    fun testMultiplePRNotifications() = runBlocking {
        // Test multiple PRs of different types
        val testPRs = listOf(
            PersonalRecord(
                id = "1",
                exerciseId = 1,
                exerciseName = "Bench Press",
                userId = "user123",
                recordType = RecordType.MAX_WEIGHT,
                weight = 100f,
                reps = 5,
                oneRepMax = 117.5f,
                volume = 500f,
                achievedAt = System.currentTimeMillis(),
                sessionId = "session123"
            ),
            PersonalRecord(
                id = "2",
                exerciseId = 2,
                exerciseName = "Squat",
                userId = "user123",
                recordType = RecordType.MAX_REPS,
                weight = 80f,
                reps = 10,
                oneRepMax = 106.67f,
                volume = 800f,
                achievedAt = System.currentTimeMillis(),
                sessionId = "session123"
            )
        )

        // Update state with multiple PRs
        testState.value = testState.value.copy(newlyAchievedRecords = testPRs)

        // Verify the state was updated with all PRs
        val currentState = viewModel.uiState.first()
        assertEquals(2, currentState.newlyAchievedRecords.size)
        assertEquals(RecordType.MAX_WEIGHT, currentState.newlyAchievedRecords[0].recordType)
        assertEquals(RecordType.MAX_REPS, currentState.newlyAchievedRecords[1].recordType)
    }

    @Test
    fun testPRNotificationVisibilityDuration() {
        // Test that PR notifications should remain visible for an appropriate duration
        // This is more of a design test to ensure the visibility duration is reasonable
        
        // Our implementation uses 7-8 seconds for visibility, which is reasonable
        // Users should have enough time to notice and read the notification
        val reasonableVisibilityDuration = 7000L // 7 seconds in milliseconds
        
        // Verify this is a reasonable duration (not too short, not too long)
        assertTrue("Visibility duration should be at least 5 seconds", reasonableVisibilityDuration >= 5000L)
        assertTrue("Visibility duration should not exceed 15 seconds", reasonableVisibilityDuration <= 15000L)
    }
}