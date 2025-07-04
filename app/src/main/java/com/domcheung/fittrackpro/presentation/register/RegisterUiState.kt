package com.domcheung.fittrackpro.presentation.register

data class RegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
){
    val canRegister: Boolean
        get() = !isLoading
}