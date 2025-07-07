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

    /**
     * Adds a new set to the current exercise.
     * The new set's details (weight, reps) are copied from the last existing set.
     */
    fun addSetToCurrentExercise() = viewModelScope.launch {
        val session = currentSession.value ?: return@launch
        val state = _uiState.value

        val exercises = session.exercises.toMutableList()
        val currentExercise = exercises.getOrNull(state.currentExerciseIndex) ?: return@launch

        val plannedSets = currentExercise.plannedSets.toMutableList()
        val lastSet = plannedSets.lastOrNull() ?: return@launch // Cannot add a set if none exist

        // Create a new set by copying the last one
        val newSet = lastSet.copy(setNumber = plannedSets.size + 1)
        plannedSets.add(newSet)

        // Update the exercise with the new list of planned sets
        exercises[state.currentExerciseIndex] = currentExercise.copy(plannedSets = plannedSets)

        // Create the final updated session object
        val updatedSession = session.copy(exercises = exercises)

        // Save the changes to the database
        updateWorkoutSessionUseCase(updatedSession)
    }
    /**
     * TODO: Implement this function in a future step.
     * Adds a new exercise to the current workout session.
     */
    fun addExerciseToCurrentSession(/*... parameters for new exercise ...*/) {
        // --- FUTURE LOGIC REMINDER ---
        // When a new exercise is added, we must ensure the "all sets completed"
        // flag is reset, so the final complete button disappears again.
        // _uiState.update { it.copy(isAllSetsCompleted = false) }
        // ---
    }

    /**
     * Removes a set from the current exercise with intelligent edge case handling.
     * - If it's the last set of the last exercise, it triggers the abandon flow.
     * - If it's the last set of a non-final exercise, it removes the whole exercise and moves to the next.
     * - Otherwise, it just removes the last set.
     */
    fun removeSetFromCurrentExercise() = viewModelScope.launch {
        val session = currentSession.value ?: return@launch
        val state = _uiState.value

        val exercises = session.exercises.toMutableList()
        val currentExercise = exercises.getOrNull(state.currentExerciseIndex) ?: return@launch
        val plannedSets = currentExercise.plannedSets.toMutableList()

        val isLastExerciseInPlan = state.currentExerciseIndex >= exercises.size - 1
        val isOnlyOneSetInExercise = plannedSets.size == 1

        // --- NEW LOGIC: Handle all edge cases ---

        // Case 1: Trying to remove the very last set of the entire workout plan.
        if (isLastExerciseInPlan && isOnlyOneSetInExercise) {
            // This action is equivalent to abandoning the workout.
            showAbandonDialog()
            return@launch
        }

        // Case 2: Trying to remove the last set of an exercise that is NOT the last in the plan.
        if (isOnlyOneSetInExercise) {
            // This action means the user wants to skip this entire exercise.
            // Remove the current exercise from the list.
            exercises.removeAt(state.currentExerciseIndex)

            // The exercise index does not need to be changed, as the next exercise
            // will automatically shift into the current index.
            // We just need to update the session with the modified exercises list.
            val updatedSession = session.copy(exercises = exercises)
            updateWorkoutSessionUseCase(updatedSession)

            // Also, reset the set index and pre-fill inputs for the new current exercise.
            val newCurrentExercise = updatedSession.exercises.getOrNull(state.currentExerciseIndex)
            if (newCurrentExercise != null) {
                _uiState.update {
                    it.copy(
                        currentSetIndex = 0,
                        currentWeight = newCurrentExercise.plannedSets.first().targetWeight,
                        currentReps = newCurrentExercise.plannedSets.first().targetReps
                    )
                }
            }
            return@launch
        }

        // --- Standard Case ---
        // The exercise has more than one set, so we just remove the last one.
        plannedSets.removeAt(plannedSets.lastIndex)
        exercises[state.currentExerciseIndex] = currentExercise.copy(plannedSets = plannedSets)
        val updatedSession = session.copy(exercises = exercises)
        updateWorkoutSessionUseCase(updatedSession)
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
