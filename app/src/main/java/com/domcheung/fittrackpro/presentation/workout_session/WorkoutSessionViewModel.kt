package com.domcheung.fittrackpro.presentation.workout_session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.model.ExecutedSet
import com.domcheung.fittrackpro.data.model.WorkoutSession
import com.domcheung.fittrackpro.data.model.WorkoutStatus
import com.domcheung.fittrackpro.domain.usecase.*
import com.domcheung.fittrackpro.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.delay

/**
 * ViewModel for the WorkoutSessionScreen.
 * Handles all business logic for an active workout session, including:
 * - The dual-timer system (overall duration and rest countdown).
 * - State management for the session (running, paused, resting).
 * - Handling user interactions like completing sets, skipping, and pausing.
 * - Updating the session data in the database.
 */
@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getWorkoutSessionByIdFlowUseCase: GetWorkoutSessionByIdFlowUseCase,
    private val updateWorkoutSessionUseCase: UpdateWorkoutSessionUseCase,
    private val completeWorkoutSessionUseCase: CompleteWorkoutSessionUseCase,
    private val pauseWorkoutSessionUseCase: PauseWorkoutSessionUseCase,
    private val resumeWorkoutSessionUseCase: ResumeWorkoutSessionUseCase,
    private val abandonWorkoutSessionUseCase: AbandonWorkoutSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutSessionState())
    val uiState: StateFlow<WorkoutSessionState> = _uiState.asStateFlow()

    private val sessionId: String = savedStateHandle.get<String>(Routes.WORKOUT_SESSION_ARG_ID)!!

    // This is the single source of truth for session data, reacting to database changes.
    val currentSession: StateFlow<WorkoutSession?> = getWorkoutSessionByIdFlowUseCase(sessionId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    companion object {
        // 3 hours in milliseconds, for the auto-pause feature.
        private const val MAX_WORKOUT_DURATION = 3 * 60 * 60 * 1000L
    }

    init {
        // A single reactive loop to handle all time and state updates.
        initializeReactiveUpdaters()
    }

    private fun initializeReactiveUpdaters() {
        viewModelScope.launch {
            // A simple flow that ticks every second to drive UI updates.
            val ticker = flow {
                while (true) {
                    emit(Unit)
                    delay(1000)
                }
            }

            // Combine the session data with the ticker.
            // This block will re-execute whenever the session data changes OR every second.
            currentSession.filterNotNull().combine(ticker) { session, _ -> session }
                .collect { session ->
                    val state = _uiState.value

                    // --- Timer A: Overall Workout Duration ---
                    val elapsedTime = if (session.status == WorkoutStatus.PAUSED) {
                        (session.pauseStartTime ?: System.currentTimeMillis()) - session.startTime - session.pausedDuration
                    } else {
                        System.currentTimeMillis() - session.startTime - session.pausedDuration
                    }

                    // --- Timer B: Rest Countdown ---
                    var restTimeRemaining = state.restTimeRemaining
                    if (state.isCurrentlyResting && session.status != WorkoutStatus.PAUSED) {
                        if (restTimeRemaining >= 1000) {
                            restTimeRemaining -= 1000
                        } else {
                            restTimeRemaining = 0
                            endRestAndMoveOn() // Rest is over
                        }
                    }

                    // --- Update the entire UI State at once ---
                    _uiState.update {
                        it.copy(
                            elapsedTime = elapsedTime.coerceAtLeast(0L),
                            restTimeRemaining = restTimeRemaining,
                            isLoading = false,
                            isCurrentlyResting = state.isCurrentlyResting && restTimeRemaining > 0 // Only show rest screen if time > 0
                        )
                    }

                    // --- Timer Limit Check ---
                    if (elapsedTime > MAX_WORKOUT_DURATION && session.status == WorkoutStatus.IN_PROGRESS) {
                        pauseWorkout()
                    }
                }
        }
    }

    /**
     * Completes the current set, saves data, and starts the rest timer.
     */
    fun completeCurrentSet() = viewModelScope.launch {
        val session = currentSession.value ?: return@launch
        val updatedSession = updateSetInData(session, isSkipped = false)
        updateWorkoutSessionUseCase(updatedSession)

        val currentExercise = updatedSession.exercises.getOrNull(_uiState.value.currentExerciseIndex)
        val restTime = currentExercise?.restBetweenSets?.toLong()?.times(1000) ?: 90000L

        _uiState.update { it.copy(isCurrentlyResting = true, restTimeRemaining = restTime, totalRestTime = restTime) }
    }

    /**
     * Skips the current set and moves to the next step immediately without resting.
     */
    fun skipCurrentSet() = viewModelScope.launch {
        val session = currentSession.value ?: return@launch
        val updatedSession = updateSetInData(session, isSkipped = true)
        updateWorkoutSessionUseCase(updatedSession)
        endRestAndMoveOn()
    }

    /**
     * Called when rest is over or skipped. Resets rest state and moves to the next step.
     */
    private fun endRestAndMoveOn() {
        _uiState.update { it.copy(isCurrentlyResting = false, restTimeRemaining = 0) }
        viewModelScope.launch {
            val session = currentSession.value ?: return@launch
            moveToNextStep(session)
        }
    }

    /**
     * Contains the logic to advance to the next set or the next exercise.
     */
    private fun moveToNextStep(session: WorkoutSession) {
        val state = _uiState.value
        val currentExercise = session.exercises.getOrNull(state.currentExerciseIndex) ?: return

        val isLastSet = state.currentSetIndex >= currentExercise.plannedSets.size - 1
        val isLastExercise = state.currentExerciseIndex >= session.exercises.size - 1

        if (isLastSet) {
            // All sets for the current exercise are done.
            if (!isLastExercise) {
                // Move to the next EXERCISE
                val nextExerciseIndex = state.currentExerciseIndex + 1
                val nextExercise = session.exercises[nextExerciseIndex]
                _uiState.update {
                    it.copy(
                        currentExerciseIndex = nextExerciseIndex,
                        currentSetIndex = 0, // Reset set index for new exercise
                        currentWeight = nextExercise.plannedSets.first().targetWeight,
                        currentReps = nextExercise.plannedSets.first().targetReps
                    )
                }
            }
            // If it's the last exercise AND the last set, do nothing. User must click "Complete Workout".
        } else {
            // Move to the next SET
            val nextSetIndex = state.currentSetIndex + 1
            val nextSet = currentExercise.plannedSets[nextSetIndex]
            _uiState.update {
                it.copy(
                    currentSetIndex = nextSetIndex,
                    currentWeight = nextSet.targetWeight,
                    currentReps = nextSet.targetReps
                )
            }
        }
    }

    /**
     * Helper to immutably update the session object with the latest set data.
     */
    private fun updateSetInData(session: WorkoutSession, isSkipped: Boolean): WorkoutSession {
        val state = _uiState.value
        val exercises = session.exercises.toMutableList()
        val currentExercise = exercises.getOrNull(state.currentExerciseIndex) ?: return session

        val executedSets = currentExercise.executedSets.toMutableList()
        val setNumber = state.currentSetIndex + 1

        val newSet = ExecutedSet(
            setNumber = setNumber,
            plannedWeight = currentExercise.plannedSets.getOrNull(state.currentSetIndex)?.targetWeight ?: 0f,
            plannedReps = currentExercise.plannedSets.getOrNull(state.currentSetIndex)?.targetReps ?: 0,
            actualWeight = if (isSkipped) 0f else state.currentWeight,
            actualReps = if (isSkipped) 0 else state.currentReps,
            isCompleted = !isSkipped,
            isSkipped = isSkipped,
            completedAt = System.currentTimeMillis()
        )

        executedSets.removeAll { it.setNumber == setNumber }
        executedSets.add(newSet)

        exercises[state.currentExerciseIndex] = currentExercise.copy(executedSets = executedSets.sortedBy { it.setNumber })

        return session.copy(exercises = exercises)
    }

    // --- Public Event Handlers for UI actions ---

    fun pauseWorkout() = viewModelScope.launch { pauseWorkoutSessionUseCase(sessionId, false) }

    fun resumeWorkout() = viewModelScope.launch { resumeWorkoutSessionUseCase(sessionId) }

    fun skipRest() = endRestAndMoveOn()

    fun adjustRestTime(adjustment: Int) {
        _uiState.update {
            val newTime = it.restTimeRemaining + (adjustment * 1000)
            it.copy(restTimeRemaining = newTime.coerceAtLeast(0L))
        }
    }

    fun abandonWorkout() {
        viewModelScope.launch {
            abandonWorkoutSessionUseCase(sessionId)
            _uiState.update { it.copy(workoutAbandoned = true) }
        }
    }

    fun completeWorkout() {
        viewModelScope.launch {
            completeWorkoutSessionUseCase(sessionId)
            _uiState.update { it.copy(workoutCompleted = true) }
        }
    }

    fun updateCurrentWeight(weight: Float) {
        _uiState.update { it.copy(currentWeight = weight) }
    }

    fun updateCurrentReps(reps: Int) {
        _uiState.update { it.copy(currentReps = reps) }
    }

    fun addSetToCurrentExercise() {
        // TODO: Implement logic to add a new PlannedSet to the current exercise
    }

    fun removeSetFromCurrentExercise() {
        // TODO: Implement logic to remove the last PlannedSet from the current exercise
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // --- Dialog Handlers ---

    fun showAbandonDialog() { _uiState.update { it.copy(showAbandonDialog = true) } }
    fun hideAbandonDialog() { _uiState.update { it.copy(showAbandonDialog = false) } }
    fun showCompleteDialog() { _uiState.update { it.copy(showCompleteDialog = true) } }
    fun hideCompleteDialog() { _uiState.update { it.copy(showCompleteDialog = false) } }
    fun showSettingsDialog() { _uiState.update { it.copy(showSettingsDialog = true) } }
    fun hideSettingsDialog() { _uiState.update { it.copy(showSettingsDialog = false) } }
    fun showReplaceExerciseDialog() { _uiState.update { it.copy(showReplaceExerciseDialog = true) } }
    fun hideReplaceExerciseDialog() { _uiState.update { it.copy(showReplaceExerciseDialog = false) } }
}