package com.domcheung.fittrackpro.presentation.plan_builder

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel for the Plan Builder screen.
 * Manages the state and logic for creating and editing a workout plan.
 */
@HiltViewModel
class PlanBuilderViewModel @Inject constructor(
    // We will inject UseCases here in later steps
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanBuilderState())
    val uiState: StateFlow<PlanBuilderState> = _uiState

    // Functions to handle UI events will be added here later.
}