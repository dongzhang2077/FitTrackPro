package com.domcheung.fittrackpro.presentation.workout_session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.model.WorkoutSession
import com.domcheung.fittrackpro.data.repository.AuthRepository
import com.domcheung.fittrackpro.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    private val getWorkoutSessionUseCase: GetWorkoutSessionUseCase, // You need to create this UseCase
    private val updateWorkoutProgressUseCase: UpdateWorkoutProgressUseCase,
    private val completeWorkoutSessionUseCase: CompleteWorkoutSessionUseCase,
    private val pauseWorkoutSessionUseCase: PauseWorkoutSessionUseCase,
    private val resumeWorkoutSessionUseCase: ResumeWorkoutSessionUseCase,
    private val abandonWorkoutSessionUseCase: AbandonWorkoutSessionUseCase, // You need to create this UseCase
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutSessionState())
    val uiState: StateFlow<WorkoutSessionState> = _uiState.asStateFlow()

    private val _currentSession = MutableStateFlow<WorkoutSession?>(null)
    val currentSession: StateFlow<WorkoutSession?> = _currentSession.asStateFlow()

    private var timerJob: Job? = null

    /**
     * Public method to load a session, called from the UI.
     */
    fun loadWorkoutSession(sessionId: String) {
        viewModelScope.launch {
            val session = getWorkoutSessionUseCase(sessionId) // This use case needs to be created
            _currentSession.value = session
            if (session != null) {
                // Initialize state from loaded session
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentExerciseIndex = 0, // Or find the last incomplete one
                    currentSetIndex = 0,
                    currentWeight = session.exercises.firstOrNull()?.plannedSets?.firstOrNull()?.targetWeight ?: 0f,
                    currentReps = session.exercises.firstOrNull()?.plannedSets?.firstOrNull()?.targetReps ?: 0
                )
                startTimer()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Workout session not found")
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val session = _currentSession.value ?: break
                val state = _uiState.value

                if (session.status == com.domcheung.fittrackpro.data.model.WorkoutStatus.IN_PROGRESS) {
                    if (state.isResting) {
                        if (state.restTimeRemaining > 0) {
                            _uiState.value = state.copy(restTimeRemaining = state.restTimeRemaining - 1000)
                        } else {
                            // Rest finished, move to next set/exercise
                            moveToNextSet()
                        }
                    } else {
                        // Update elapsed time
                        val newElapsedTime = System.currentTimeMillis() - session.startTime - session.pausedDuration
                        _uiState.value = state.copy(elapsedTime = newElapsedTime)
                    }
                }
            }
        }
    }

    private fun moveToNextSet() {
        // TODO: Implement logic to move to the next set or exercise after rest
        _uiState.value = _uiState.value.copy(isResting = false)
    }

    fun completeCurrentSet() {
        // TODO: Implement the logic for completing a set
        // It should update the session, check for personal records, and start the rest timer.
        val restTime = _currentSession.value?.exercises?.getOrNull(_uiState.value.currentExerciseIndex)?.restBetweenSets?.toLong()?.times(1000) ?: 90000L
        _uiState.value = _uiState.value.copy(isResting = true, restTimeRemaining = restTime, totalRestTime = restTime)
    }

    // --- Public event handlers ---
    fun pauseWorkout() = viewModelScope.launch { _currentSession.value?.let { pauseWorkoutSessionUseCase(it.id) } }
    fun resumeWorkout() = viewModelScope.launch { _currentSession.value?.let { resumeWorkoutSessionUseCase(it.id) } }
    fun abandonWorkout() = viewModelScope.launch { _currentSession.value?.let { abandonWorkoutSessionUseCase(it.id); _uiState.value = _uiState.value.copy(workoutAbandoned = true) } }
    fun completeWorkout() = viewModelScope.launch { _currentSession.value?.let { completeWorkoutSessionUseCase(it.id); _uiState.value = _uiState.value.copy(workoutCompleted = true) } }
    fun skipCurrentSet() { /* TODO */ }
    fun skipRest() { moveToNextSet() }
    fun adjustRestTime(adjustment: Int) { _uiState.update { it.copy(restTimeRemaining = (it.restTimeRemaining + adjustment * 1000).coerceAtLeast(0)) } }
    fun updateCurrentWeight(weight: Float) { _uiState.update { it.copy(currentWeight = weight) } }
    fun updateCurrentReps(reps: Int) { _uiState.update { it.copy(currentReps = reps) } }
    fun addSetToCurrentExercise() { /* TODO */ }
    fun removeSetFromCurrentExercise() { /* TODO */ }

    // --- Dialog handlers ---
    fun showAbandonDialog() { _uiState.update { it.copy(showAbandonDialog = true) } }
    fun hideAbandonDialog() { _uiState.update { it.copy(showAbandonDialog = false) } }
    fun showCompleteDialog() { _uiState.update { it.copy(showCompleteDialog = true) } }
    fun hideCompleteDialog() { _uiState.update { it.copy(showCompleteDialog = false) } }
    fun showSettingsDialog() { _uiState.update { it.copy(showSettingsDialog = true) } }
    fun hideSettingsDialog() { _uiState.update { it.copy(showSettingsDialog = false) } }
    fun showReplaceExerciseDialog() { _uiState.update { it.copy(showReplaceExerciseDialog = true) } }

    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

// NOTE: You will need to create the following UseCases if they don't exist:
// - GetWorkoutSessionUseCase(repository) -> suspend operator fun invoke(sessionId: String): WorkoutSession?
// - AbandonWorkoutSessionUseCase(repository) -> suspend operator fun invoke(sessionId: String): Result<Unit>