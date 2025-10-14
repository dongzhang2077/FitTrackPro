package com.domcheung.fittrackpro.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.local.UserPreferencesManager
import com.domcheung.fittrackpro.presentation.onboarding.OnboardingData
import com.domcheung.fittrackpro.presentation.onboarding.ExperienceLevel
import com.domcheung.fittrackpro.presentation.onboarding.FitnessGoal
import com.domcheung.fittrackpro.presentation.onboarding.WorkoutFrequency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    // Current page index
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    // Onboarding data
    private val _onboardingData = MutableStateFlow(OnboardingData())
    val onboardingData: StateFlow<OnboardingData> = _onboardingData.asStateFlow()

    // Total pages
    val totalPages = 3

    init {
        // Pre-populate with some default values
        _onboardingData.value = OnboardingData()
    }

    /**
     * Navigate to next page
     */
    fun nextPage() {
        val currentPageIndex = _currentPage.value
        if (currentPageIndex < totalPages - 1) {
            if (validateCurrentPage(currentPageIndex)) {
                _currentPage.value = currentPageIndex + 1
                _uiState.value = _uiState.value.copy(pageError = null)
            }
        }
    }

    /**
     * Navigate to previous page
     */
    fun previousPage() {
        val currentPageIndex = _currentPage.value
        if (currentPageIndex > 0) {
            _currentPage.value = currentPageIndex - 1
            _uiState.value = _uiState.value.copy(pageError = null)
        }
    }

    /**
     * Update selected avatar
     */
    fun updateSelectedAvatar(avatar: DefaultAvatar) {
        _onboardingData.value = _onboardingData.value.copy(selectedAvatar = avatar)
    }

    /**
     * Update height
     */
    fun updateHeight(height: String) {
        _onboardingData.value = _onboardingData.value.copy(
            height = sanitizeMeasurementInput(height)
        )
    }

    /**
     * Update current weight
     */
    fun updateCurrentWeight(weight: String) {
        _onboardingData.value = _onboardingData.value.copy(
            currentWeight = sanitizeMeasurementInput(weight)
        )
    }

    /**
     * Update target weight
     */
    fun updateTargetWeight(weight: String) {
        _onboardingData.value = _onboardingData.value.copy(
            targetWeight = sanitizeMeasurementInput(weight)
        )
    }

    /**
     * Update experience level
     */
    fun updateExperienceLevel(level: ExperienceLevel) {
        _onboardingData.value = _onboardingData.value.copy(experienceLevel = level)
    }

    /**
     * Update primary goal
     */
    fun updatePrimaryGoal(goal: FitnessGoal) {
        _onboardingData.value = _onboardingData.value.copy(primaryGoal = goal)
    }

    /**
     * Update workout frequency
     */
    fun updateWorkoutFrequency(frequency: WorkoutFrequency) {
        _onboardingData.value = _onboardingData.value.copy(workoutFrequency = frequency)
    }

    /**
     * Complete onboarding and save data
     */
    fun completeOnboarding() {
        if (!validateCurrentPage(_currentPage.value)) {
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val data = _onboardingData.value

                // Save all data to UserPreferencesManager
                val avatarString = data.selectedAvatar?.let {
                    "${it.id}:${it.emoji}:${it.color.value.toString()}"
                } ?: ""

                // Don't overwrite the name during onboarding as it's already saved during registration
                userPreferencesManager.saveUserProfile(
                    name = null, // Pass null to avoid overwriting the existing name
                    avatarUrl = avatarString,
                    currentWeight = data.currentWeight,
                    targetWeight = data.targetWeight,
                    height = data.height,
                    initialWeight = data.currentWeight // Set initial weight to current weight
                )

                // Mark onboarding as completed
                _onboardingData.value = data.copy(isCompleted = true)

                // Mark onboarding as completed in preferences
                userPreferencesManager.setOnboardingCompleted(true)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isCompleted = true,
                    pageError = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pageError = "Failed to save profile: ${e.message}"
                )
            }
        }
    }

    /**
     * Skip onboarding (optional for users who want to set up later)
     */
    fun skipOnboarding() {
        _uiState.value = _uiState.value.copy(isSkipped = true)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(pageError = null)
    }

    /**
     * Validate current page data
     */
    private fun validateCurrentPage(pageIndex: Int): Boolean {
        val data = _onboardingData.value

        return when (pageIndex) {
            0 -> true // Avatar page - avatar is optional
            1 -> { // Physical data page
                when {
                    data.height.isBlank() -> {
                        _uiState.value = _uiState.value.copy(pageError = "Please enter your height")
                        false
                    }
                    data.currentWeight.isBlank() -> {
                        _uiState.value = _uiState.value.copy(pageError = "Please enter your current weight")
                        false
                    }
                    data.targetWeight.isBlank() -> {
                        _uiState.value = _uiState.value.copy(pageError = "Please enter your target weight")
                        false
                    }
                    else -> true
                }
            }
            2 -> true // Goals page is always valid (has defaults)
            else -> true
        }
    }

    /**
     * Get progress percentage
     */
    fun getProgress(): Float {
        return (_currentPage.value + 1).toFloat() / totalPages
    }

    private fun sanitizeMeasurementInput(input: String): String {
        var decimalFound = false
        val sanitized = buildString {
            input.forEach { char ->
                when {
                    char.isDigit() -> append(char)
                    char == '.' && !decimalFound -> {
                        append(char)
                        decimalFound = true
                    }
                }
            }
        }.trim()

        return sanitized.trimEnd { it == '.' }
    }
}

/**
 * UI State for Onboarding screen
 */
data class OnboardingUiState(
    val isLoading: Boolean = false,
    val pageError: String? = null,
    val isCompleted: Boolean = false,
    val isSkipped: Boolean = false
)
