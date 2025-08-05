package com.domcheung.fittrackpro.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.repository.AuthRepository
import com.domcheung.fittrackpro.domain.usecase.SyncExercisesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
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

    // The init block is automatically executed when the ViewModel is created.
    init {
        // Start the entire app initialization process from here.
        initializeApp()
    }

    /**
     * This is the single entry point for app initialization.
     * It runs automatically when the ViewModel is created.
     */
    private fun initializeApp() {
        viewModelScope.launch {
            try {
                // Ensure the splash screen is visible for at least 2 seconds for branding.
                delay(2000)

                // First, perform the critical data sync/seed operation.
                syncExercisesUseCase()

                // After syncing, check if the user is already logged in.
                val isLoggedIn = authRepository.checkInitialLoginState()

                // Finally, update the state to trigger navigation.
                _uiState.value = SplashUiState(
                    isLoading = false,
                    navigationTarget = if (isLoggedIn) NavigationTarget.MAIN else NavigationTarget.LOGIN
                )
            } catch (e: Exception) {
                // If anything fails (sync or auth check), navigate to login as a safe fallback.
                _uiState.value = SplashUiState(
                    isLoading = false,
                    navigationTarget = NavigationTarget.LOGIN
                )
            }
        }
    }
}