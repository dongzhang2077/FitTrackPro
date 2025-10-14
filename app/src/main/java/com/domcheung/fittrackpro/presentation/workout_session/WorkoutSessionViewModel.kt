package com.domcheung.fittrackpro.presentation.workout_session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.model.ExecutedExercise
import com.domcheung.fittrackpro.data.model.ExecutedSet
import com.domcheung.fittrackpro.data.model.PlannedSet
import com.domcheung.fittrackpro.data.model.WeightUnit
import com.domcheung.fittrackpro.data.model.WorkoutSession
import com.domcheung.fittrackpro.data.model.WorkoutStatus
import com.domcheung.fittrackpro.domain.usecase.*
import com.domcheung.fittrackpro.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getWorkoutSessionByIdFlowUseCase: GetWorkoutSessionByIdFlowUseCase,
    private val updateWorkoutSessionUseCase: UpdateWorkoutSessionUseCase,
    private val completeWorkoutSessionUseCase: CompleteWorkoutSessionUseCase,
    private val pauseWorkoutSessionUseCase: PauseWorkoutSessionUseCase,
    private val resumeWorkoutSessionUseCase: ResumeWorkoutSessionUseCase,
    private val abandonWorkoutSessionUseCase: AbandonWorkoutSessionUseCase,
    private val checkAndCreatePersonalRecordUseCase: CheckAndCreatePersonalRecordUseCase,
    private val getExercisesByIdsUseCase: GetExercisesByIdsUseCase
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
                    _uiState.update { currentState ->
                        val elapsedTime = if (session.status != WorkoutStatus.PAUSED) {
                            System.currentTimeMillis() - session.startTime - session.pausedDuration
                        } else {
                            (session.pauseStartTime ?: System.currentTimeMillis()) - session.startTime - session.pausedDuration
                        }

                        var restTimeRemaining = currentState.restTimeRemaining
                        if (session.status == WorkoutStatus.RESTING) {
                            if (restTimeRemaining >= 1000) {
                                restTimeRemaining -= 1000
                            } else {
                                restTimeRemaining = 0
                                resumeWorkout()
                            }
                        }

                        val exercise = session.exercises.getOrNull(currentState.currentExerciseIndex)
                        val set = exercise?.plannedSets?.getOrNull(currentState.currentSetIndex)

                        val weightToDisplay = if (currentState.currentWeight == 0f && currentState.currentReps == 0) {
                            set?.targetWeight ?: 0f
                        } else {
                            currentState.currentWeight
                        }
                        val repsToDisplay = if (currentState.currentWeight == 0f && currentState.currentReps == 0) {
                            set?.targetReps ?: 0
                        } else {
                            currentState.currentReps
                        }

                        currentState.copy(
                            isLoading = false,
                            isCurrentlyResting = session.status == WorkoutStatus.RESTING,
                            elapsedTime = elapsedTime.coerceAtLeast(0L),
                            restTimeRemaining = restTimeRemaining,
                            currentWeight = weightToDisplay,
                            currentReps = repsToDisplay
                        )
                    }

                    if (_uiState.value.elapsedTime > MAX_WORKOUT_DURATION && session.status == WorkoutStatus.IN_PROGRESS) {
                        pauseWorkout()
                    }
                }
        }
    }

    fun completeCurrentSet() = viewModelScope.launch {
        val state = _uiState.value

        if (state.currentWeight <= 0 || state.currentReps <= 0) {
            _uiState.update { it.copy(
                showInvalidInputDialog = true,
                inputErrorMessage = "Weight and reps must be positive numbers."
            )}
            return@launch
        }

        val session = currentSession.value ?: return@launch
        val userId = session.userId
        val currentExercise = session.exercises.getOrNull(state.currentExerciseIndex) ?: return@launch

        val updatedSession = updateSetInData(session, isSkipped = false)
        updateWorkoutSessionUseCase(updatedSession)

        val prResult = checkAndCreatePersonalRecordUseCase(
            userId = userId,
            exerciseId = currentExercise.exerciseId,
            exerciseName = currentExercise.exerciseName,
            weight = state.currentWeight,
            reps = state.currentReps,
            sessionId = session.id
        )
        if (prResult.isSuccess) {
            val newRecords = prResult.getOrNull()
            if (!newRecords.isNullOrEmpty()) {
                // Clear any previous notifications before showing new ones
                _uiState.update { it.copy(newlyAchievedRecords = emptyList()) }
                // Add a small delay to ensure the state change is picked up
                kotlinx.coroutines.delay(100)
                // Now set the new records
                _uiState.update { it.copy(newlyAchievedRecords = newRecords) }
                // Auto-clear notifications after a longer period
                viewModelScope.launch {
                    kotlinx.coroutines.delay(10000) // 10 seconds
                    _uiState.update { it.copy(newlyAchievedRecords = emptyList()) }
                }
            }
        }

        if (checkIfAllSetsAreCompleted(updatedSession)) {
            _uiState.update { it.copy(isAllSetsCompleted = true, showFinishWorkoutDialog = true) }
        } else {
            val restTime = currentExercise.restBetweenSets.toLong() * 1000
            _uiState.update { it.copy(totalRestTime = restTime, restTimeRemaining = restTime) }
            pauseWorkoutSessionUseCase(sessionId, isResting = true)
        }
    }

    fun skipCurrentSet() = viewModelScope.launch {
        val session = currentSession.value ?: return@launch
        val updatedSession = updateSetInData(session, isSkipped = true)
        updateWorkoutSessionUseCase(updatedSession)

        if (checkIfAllSetsAreCompleted(updatedSession)) {
            _uiState.update { it.copy(isAllSetsCompleted = true, showFinishWorkoutDialog = true) }
        } else {
            moveToNextStep(updatedSession)
        }
    }

    private fun checkIfAllSetsAreCompleted(session: WorkoutSession): Boolean {
        return session.exercises.all { exercise ->
            exercise.executedSets.filter { it.isCompleted }.size == exercise.plannedSets.size
        }
    }

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

    private fun updateSetInData(session: WorkoutSession, isSkipped: Boolean): WorkoutSession {
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

    fun pauseWorkout() = viewModelScope.launch { pauseWorkoutSessionUseCase(sessionId, false) }
    fun resumeWorkout() = viewModelScope.launch {
        if (currentSession.value?.status == WorkoutStatus.RESTING) {
            currentSession.value?.let { moveToNextStep(it) }
        }
        resumeWorkoutSessionUseCase(sessionId)
    }
    fun skipRest() = resumeWorkout()

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

    fun addSetToCurrentExercise() = viewModelScope.launch {
        val session = currentSession.value ?: return@launch
        val state = _uiState.value

        val exercises = session.exercises.toMutableList()
        val currentExercise = exercises.getOrNull(state.currentExerciseIndex) ?: return@launch

        val plannedSets = currentExercise.plannedSets.toMutableList()
        val lastSet = plannedSets.lastOrNull() ?: return@launch

        val newSet = lastSet.copy(setNumber = plannedSets.size + 1)
        plannedSets.add(newSet)

        exercises[state.currentExerciseIndex] = currentExercise.copy(plannedSets = plannedSets)

        val updatedSession = session.copy(exercises = exercises)

        updateWorkoutSessionUseCase(updatedSession)
    }

    fun removeSetFromCurrentExercise() = viewModelScope.launch {
        val session = currentSession.value ?: return@launch
        val state = _uiState.value

        val exercises = session.exercises.toMutableList()
        val currentExercise = exercises.getOrNull(state.currentExerciseIndex) ?: return@launch
        val plannedSets = currentExercise.plannedSets.toMutableList()

        val isLastExerciseInPlan = state.currentExerciseIndex >= exercises.size - 1
        val isOnlyOneSetInExercise = plannedSets.size == 1

        if (isLastExerciseInPlan && isOnlyOneSetInExercise) {
            showAbandonDialog()
            return@launch
        }

        if (isOnlyOneSetInExercise) {
            exercises.removeAt(state.currentExerciseIndex)

            val updatedSession = session.copy(exercises = exercises)
            updateWorkoutSessionUseCase(updatedSession)

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

        plannedSets.removeAt(plannedSets.lastIndex)
        exercises[state.currentExerciseIndex] = currentExercise.copy(plannedSets = plannedSets)
        val updatedSession = session.copy(exercises = exercises)
        updateWorkoutSessionUseCase(updatedSession)
    }

    fun toggleWeightUnit() {
        _uiState.update {
            val newUnit = if (it.weightUnit == WeightUnit.KG) WeightUnit.LB else WeightUnit.KG
            it.copy(weightUnit = newUnit)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun showAbandonDialog() { _uiState.update { it.copy(showAbandonDialog = true) } }
    fun hideAbandonDialog() { _uiState.update { it.copy(showAbandonDialog = false) } }
    fun showCompleteDialog() { _uiState.update { it.copy(showCompleteDialog = true) } }
    fun hideCompleteDialog() { _uiState.update { it.copy(showCompleteDialog = false) } }
    fun showSettingsDialog() { _uiState.update { it.copy(showSettingsDialog = true) } }
    fun hideSettingsDialog() { _uiState.update { it.copy(showSettingsDialog = false) } }
    fun showReplaceExerciseDialog() { _uiState.update { it.copy(showReplaceExerciseDialog = true) } }
    fun hideReplaceExerciseDialog() { _uiState.update { it.copy(showReplaceExerciseDialog = false) } }
    fun hideFinishWorkoutDialog() {
        _uiState.update { it.copy(showFinishWorkoutDialog = false) }
    }

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

    fun hideInvalidInputDialog() {
        _uiState.update { it.copy(showInvalidInputDialog = false, inputErrorMessage = null) }
    }

    fun onExerciseSelected(exerciseIndex: Int) {
        val session = currentSession.value ?: return
        val selectedExercise = session.exercises.getOrNull(exerciseIndex) ?: return

        _uiState.update {
            it.copy(
                currentExerciseIndex = exerciseIndex,
                currentSetIndex = 0,
                currentWeight = selectedExercise.plannedSets.firstOrNull()?.targetWeight ?: 0f,
                currentReps = selectedExercise.plannedSets.firstOrNull()?.targetReps ?: 0
            )
        }
    }

    fun showExerciseLibrary() {
        _uiState.update { it.copy(showExerciseLibrary = true) }
    }

    fun hideExerciseLibrary() {
        _uiState.update { it.copy(showExerciseLibrary = false) }
    }

    fun addExercisesToCurrentSession(selectedIds: Set<Int>) {
        viewModelScope.launch {
            val session = currentSession.value ?: return@launch
            val exercisesToAdd = getExercisesByIdsUseCase(selectedIds.toList())

            val newExecutedExercises = exercisesToAdd.map { exercise ->
                ExecutedExercise(
                    exerciseId = exercise.id,
                    exerciseName = exercise.name,
                    orderIndex = session.exercises.size,
                    plannedSets = listOf(PlannedSet(setNumber = 1, targetWeight = 20f, targetReps = 10)),
                    executedSets = emptyList(),
                    imageUrl = exercise.imageUrl,
                    videoUrl = exercise.videoUrl
                )
            }

            val updatedExercises = session.exercises + newExecutedExercises
            val updatedSession = session.copy(exercises = updatedExercises, isPlanModified = true)
            updateWorkoutSessionUseCase(updatedSession)
            _uiState.update { it.copy(isAllSetsCompleted = false) }
        }
    }
}
