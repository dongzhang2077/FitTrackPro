package com.domcheung.fittrackpro.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.model.User
import com.domcheung.fittrackpro.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    private fun validateInput(email: String, password: String, confirmPassword: String): String? {
        return when {
            email.isBlank() -> "Email cannot be empty"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Please enter a valid email address"
            password.length < 6 -> "Password must be at least 6 characters"
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }

    fun registerUser(email: String, password: String, confirmPassword: String, user: User) {
        val validationError = validateInput(email, password, confirmPassword)
        if (validationError != null) {
            _uiState.value = RegisterUiState(errorMessage = validationError)
            return
        }

        _uiState.value = RegisterUiState(isLoading = true)

        viewModelScope.launch {
            authRepository.registerUser(email, password, user).collect { result ->
                result
                    .onSuccess {
                        _uiState.value = RegisterUiState(isSuccess = true)
                    }
                    .onFailure { exception ->
                        _uiState.value = RegisterUiState(errorMessage = exception.message ?: "Registration failed")
                    }
            }
        }
    }
}