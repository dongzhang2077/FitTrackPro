package com.domcheung.fittrackpro.presentation.workout_session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.model.ExecutedExercise
import com.domcheung.fittrackpro.data.model.ExecutedSet
import com.domcheung.fittrackpro.data.model.WorkoutSession
import com.domcheung.fittrackpro.data.model.WorkoutStatus
import com.domcheung.fittrackpro.domain.usecase.*
import com.domcheung.fittrackpro.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val abandonWorkoutSessionUseCase: AbandonWorkoutSessionUseCase,
    private val checkAndCreatePersonalRecordUseCase: CheckAndCreatePersonalRecordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutSessionState())
    val uiState: StateFlow<WorkoutSessionState> = _uiState.asStateFlow()

    private val sessionId: String = savedStateHandle.get<String>(Routes.WORKOUT_SESSION_ARG_ID)!!

    val currentSession: StateFlow<WorkoutSession?> = getWorkoutSessionByIdFlowUseCase(sessionId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    companion object {
        private const val MAX_WORKOUT_DURATION = 3 * 60 * 60 * 1000L // 3 hours
    }

    init {
        initializeReactiveUpdaters()
    }

    private fun initializeReactiveUpdaters() {
        viewModelScope.launch {
            val ticker = flow {
                while (true) {
                    emit(Unit)
                    delay(1000)
                }
            }

            currentSession.filterNotNull().combine(ticker) { session, _ -> session }
                .collect { session ->
                    val state = _uiState.value

                    // --- NEW, ROBUST TIMER LOGIC ---
                    // Timer A (Total Duration) runs unless explicitly PAUSED.
                    val elapsedTime = if (session.status != WorkoutStatus.PAUSED) {
                        System.currentTimeMillis() - session.startTime - session.pausedDuration
                    } else {
                        (session.pauseStartTime ?: System.currentTimeMillis()) - session.startTime - session.pausedDuration
                    }

                    // Timer B (Rest Countdown) runs ONLY when status is RESTING.
                    var restTimeRemaining = state.restTimeRemaining
                    if (session.status == WorkoutStatus.RESTING) {
                        if (restTimeRemaining >= 1000) {
                            restTimeRemaining -= 1000
                        } else {
                            restTimeRemaining = 0
                            resumeWorkout() // Rest is over, resume to IN_PROGRESS
                        }
                    }

                    _uiState.update {
                        it.copy(
                            elapsedTime = elapsedTime.coerceAtLeast(0L),
                            restTimeRemaining = restTimeRemaining,
                            // The UI for resting is now directly driven by the database status.
                            isCurrentlyResting = session.status == WorkoutStatus.RESTING,
                            isLoading = false
                        )
                    }

                    if (elapsedTime > MAX_WORKOUT_DURATION && session.status == WorkoutStatus.IN_PROGRESS) {
                        pauseWorkout()
                    }
                }
        }
    }

    /**
     * Completes the current set, saves data, and puts the session into a RESTING state.
     */
    fun completeCurrentSet() = viewModelScope.launch {
        val session = currentSession.value ?: return@launch
        val state = _uiState.value
        val userId = session.userId
        val currentExercise = session.exercises.getOrNull(state.currentExerciseIndex) ?: return@launch

        // Step 1: Save data
        val updatedSession = updateSetInData(session, isSkipped = false)
        updateWorkoutSessionUseCase(updatedSession)
        println("PR_DEBUG: Set data saved for exerciseId: ${currentExercise.exerciseId}")

        // Step 2: Check for PRs
        val prResult = checkAndCreatePersonalRecordUseCase(
            userId = userId,
            exerciseId = currentExercise.exerciseId,
            weight = state.currentWeight,
            reps = state.currentReps,
            sessionId = session.id
        )
        if (prResult.isSuccess) {
            val newRecords = prResult.getOrNull()
            if (!newRecords.isNullOrEmpty()) {
                println("PR_DEBUG: ViewModel received ${newRecords.size} new records. Updating UI state.")
                _uiState.update { it.copy(newlyAchievedRecords = newRecords) }
            } else {
                println("PR_DEBUG: ViewModel received 0 new records.")
            }
        } else {
            println("PR_DEBUG: ViewModel received PR check failure: ${prResult.exceptionOrNull()?.message}")
        }

        val isLastSet = state.currentSetIndex >= currentExercise.plannedSets.size - 1
        val isLastExercise = state.currentExerciseIndex >= updatedSession.exercises.size - 1

        if (isLastSet && isLastExercise) {
            _uiState.update { it.copy(isAllSetsCompleted = true, showFinishWorkoutDialog = true) }
        } else {
            // --- NEW LOGIC: Set the session status to RESTING ---
            val restTime = currentExercise.restBetweenSets.toLong() * 1000
            _uiState.update { it.copy(totalRestTime = restTime, restTimeRemaining = restTime) }
            // This call now correctly transitions the session to a resting state.
            pauseWorkoutSessionUseCase(sessionId, isResting = true)
        }
    }

    /**
     * Skips the current set and moves to the next step immediately.
     */
    fun skipCurrentSet() = viewModelScope.launch {
        val session = currentSession.value ?: return@launch
        val updatedSession = updateSetInData(session, isSkipped = true)
        updateWorkoutSessionUseCase(updatedSession)
        moveToNextStep(updatedSession)
    }

    /**
     * Moves the workout to the next set or exercise.
     */
    private fun moveToNextStep(session: WorkoutSession) {
        val state = _uiState.value
        val currentExercise = session.exercises.getOrNull(state.currentExerciseIndex) ?: return

        val isLastSet = state.currentSetIndex >= currentExercise.plannedSets.size - 1
        val isLastExercise = state.currentExerciseIndex >= session.exercises.size - 1

        if (isLastSet) {
            if (!isLastExercise) {
                val nextExerciseIndex = state.currentExerciseIndex + 1
                val nextExercise = session.exercises[nextExerciseIndex]
                _uiState.update {
                    it.copy(
                        currentExerciseIndex = nextExerciseIndex,
                        currentSetIndex = 0,
                        currentWeight = nextExercise.plannedSets.first().targetWeight,
                        currentReps = nextExercise.plannedSets.first().targetReps
                    )
                }
            }
        } else {
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
     * A pure helper function to update session data immutably.
     */
    private fun updateSetInData(session: WorkoutSession, isSkipped: Boolean): WorkoutSession {
        // ... (This function's internal logic remains unchanged)
        val state = _uiState.value
        val exercises = session.exercises.toMutableList()
        val currentExercise = exercises.getOrNull(state.currentExerciseIndex) ?: return session
        val executedSets = currentExercise.executedSets.toMutableList()
        val setNumber = state.currentSetIndex + 1
        val newSet = ExecutedSet(setNumber = setNumber, plannedWeight = currentExercise.plannedSets.getOrNull(state.currentSetIndex)?.targetWeight ?: 0f, plannedReps = currentExercise.plannedSets.getOrNull(state.currentSetIndex)?.targetReps ?: 0, actualWeight = if (isSkipped) 0f else state.currentWeight, actualReps = if (isSkipped) 0 else state.currentReps, isCompleted = !isSkipped, isSkipped = isSkipped, completedAt = System.currentTimeMillis())
        executedSets.removeAll { it.setNumber == setNumber }
        executedSets.add(newSet)
        exercises[state.currentExerciseIndex] = currentExercise.copy(executedSets = executedSets.sortedBy { it.setNumber })
        val newCompletionPercentage = calculateCompletionPercentage(exercises)
        val newTotalVolume = calculateTotalVolume(exercises)
        return session.copy(exercises = exercises, completionPercentage = newCompletionPercentage, totalVolume = newTotalVolume)
    }

    // --- Public Event Handlers ---
    fun pauseWorkout() = viewModelScope.launch { pauseWorkoutSessionUseCase(sessionId, false) }
    fun resumeWorkout() = viewModelScope.launch {
        // When resuming, move to the next step if we were in a RESTING state.
        if (currentSession.value?.status == WorkoutStatus.RESTING) {
            currentSession.value?.let { moveToNextStep(it) }
        }
        resumeWorkoutSessionUseCase(sessionId)
    }
    fun skipRest() = resumeWorkout() // Skipping rest is the same as resuming immediately.

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
    /**
     * Hides the dialog that asks the user to confirm finishing the workout.
     */
    fun hideFinishWorkoutDialog() {
        _uiState.update { it.copy(showFinishWorkoutDialog = false) }
    }

    /**
     * Clears the list of newly achieved personal records from the state.
     * This should be called by the UI after the celebration animation has finished.
     */
    fun clearNewPrNotifications() {
        _uiState.update { it.copy(newlyAchievedRecords = emptyList()) }
    }

    private fun calculateCompletionPercentage(exercises: List<ExecutedExercise>): Float {
        if (exercises.isEmpty()) return 0f

        val totalPlannedSets = exercises.sumOf { it.plannedSets.size }
        val totalCompletedSets = exercises.sumOf { exercise ->
            exercise.executedSets.count { it.isCompleted }
        }

        return if (totalPlannedSets > 0) {
            (totalCompletedSets.toFloat() / totalPlannedSets.toFloat()) * 100f
        } else {
            0f
        }
    }

    private fun calculateTotalVolume(exercises: List<ExecutedExercise>): Float {
        return exercises.sumOf { exercise ->
            exercise.executedSets.filter { it.isCompleted }.sumOf { set ->
                (set.actualWeight * set.actualReps).toDouble()
            }
        }.toFloat()
    }
}
