package com.domcheung.fittrackpro.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import com.domcheung.fittrackpro.domain.usecase.SyncExercisesUseCase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NavigationTarget {
    NONE,    // Still checking
    LOGIN,   // Not logged in, go to login
    MAIN     // Logged in, go to main app
}

data class SplashUiState(
    val isLoading: Boolean = true,
    val navigationTarget: NavigationTarget = NavigationTarget.NONE
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncExercisesUseCase: SyncExercisesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState

    fun checkLoginState() {
        viewModelScope.launch {
            try {
                println("DEBUG: Checking login state...")

                // --- NEW LOGIC: Seed exercises first ---
                // Ensure sample exercises exist before proceeding.
                syncExercisesUseCase()
                println("DEBUG: Exercise sync/seed completed.")

                // Use full login state check
                val isLoggedIn = authRepository.checkInitialLoginState()

                println("DEBUG: Login check result: $isLoggedIn")

                _uiState.value = SplashUiState(
                    isLoading = false,
                    navigationTarget = if (isLoggedIn) {
                        println("DEBUG: Navigating to MAIN")
                        NavigationTarget.MAIN
                    } else {
                        println("DEBUG: Navigating to LOGIN")
                        NavigationTarget.LOGIN
                    }
                )
            } catch (e: Exception) {
                println("DEBUG: Error checking login state: ${e.message}")
                // If there's an error, default to login screen
                _uiState.value = SplashUiState(
                    isLoading = false,
                    navigationTarget = NavigationTarget.LOGIN
                )
            }
        }
    }
}