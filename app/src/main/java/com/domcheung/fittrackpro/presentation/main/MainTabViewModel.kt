package com.domcheung.fittrackpro.presentation.main

import androidx.lifecycle.ViewModel
import com.domcheung.fittrackpro.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainTabViewModel @Inject constructor(
    val authRepository: AuthRepository
) : ViewModel()