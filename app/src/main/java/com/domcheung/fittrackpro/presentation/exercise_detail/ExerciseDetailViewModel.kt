package com.domcheung.fittrackpro.presentation.exercise_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.model.Exercise
import com.domcheung.fittrackpro.domain.usecase.GetExerciseByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    private val getExerciseByIdUseCase: GetExerciseByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exerciseId: Int = checkNotNull(savedStateHandle["exerciseId"])

    private val _exercise = MutableStateFlow<Exercise?>(null)
    val exercise: StateFlow<Exercise?> = _exercise.asStateFlow()

    init {
        loadExercise()
    }

    private fun loadExercise() {
        viewModelScope.launch {
            val result = getExerciseByIdUseCase(exerciseId)
            _exercise.value = result
        }
    }
}
