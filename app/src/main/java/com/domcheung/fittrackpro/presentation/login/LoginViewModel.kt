package com.domcheung.fittrackpro.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private fun validateInput(email: String, password: String): String? {
        return when {
            email.isBlank() -> "Email cannot be empty"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Please enter a valid email address"
            password.isBlank() -> "Password cannot be empty"
            else -> null
        }
    }

    fun loginUser(email: String, password: String) {
        val validationError = validateInput(email, password)
        if (validationError != null) {
            _uiState.value = LoginUiState(errorMessage = validationError)
            return
        }

        _uiState.value = LoginUiState(isLoading = true)

        viewModelScope.launch {
            authRepository.loginUser(email, password).collect { result ->
                result
                    .onSuccess {
                        _uiState.value = LoginUiState(isSuccess = true)
                    }
                    .onFailure { exception ->
                        _uiState.value = LoginUiState(errorMessage = exception.message ?: "Login failed")
                    }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}