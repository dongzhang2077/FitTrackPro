package com.domcheung.fittrackpro.presentation.login

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val canLogin: Boolean
        get() = !isLoading
}