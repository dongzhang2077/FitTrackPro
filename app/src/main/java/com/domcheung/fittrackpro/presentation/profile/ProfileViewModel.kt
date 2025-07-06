package com.domcheung.fittrackpro.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.local.UserPreferencesManager
import com.domcheung.fittrackpro.data.repository.AuthRepository
import com.domcheung.fittrackpro.data.repository.WorkoutStatistics
import com.domcheung.fittrackpro.domain.usecase.GetWorkoutStatisticsUseCase
import com.domcheung.fittrackpro.domain.usecase.SyncDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Profile tab screen
 * Manages user profile data, settings, and account operations
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val getWorkoutStatisticsUseCase: GetWorkoutStatisticsUseCase,
    private val syncDataUseCase: SyncDataUseCase
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // User login status
    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // User email
    val userEmail: StateFlow<String> = userPreferencesManager.userEmail
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    // User name
    val userName: StateFlow<String> = userPreferencesManager.userName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    // User UID
    val userUid: StateFlow<String> = userPreferencesManager.userUid
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    // Workout statistics for profile summary
    val workoutStatistics: StateFlow<WorkoutStatistics?> = userUid
        .flatMapLatest { uid ->
            if (uid.isNotEmpty()) {
                flow<WorkoutStatistics?> {
                    val result = getWorkoutStatisticsUseCase(uid)
                    emit(result.getOrNull())
                }
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // User profile data combined
    val userProfile: StateFlow<UserProfileData> = combine(
        userName,
        userEmail,
        userUid,
        workoutStatistics
    ) { name, email, uid, stats ->
        UserProfileData(
            name = name.ifEmpty { email.substringBefore('@') },
            email = email,
            uid = uid,
            joinDate = "Member since Jan 2025", // TODO: Get actual join date
            currentWeight = "75 kg", // TODO: Get from user preferences
            targetWeight = "70 kg", // TODO: Get from user goals
            height = "175 cm", // TODO: Get from user preferences
            totalWorkouts = stats?.totalWorkouts ?: 0,
            currentStreak = stats?.currentStreak ?: 0,
            totalVolumeLifted = stats?.totalVolumeLifted ?: 0f
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfileData()
    )

    init {
        loadProfileData()
        checkSyncStatus()
    }

    /**
     * Load profile data
     */
    private fun loadProfileData() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                // Data is automatically loaded through StateFlow
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load profile data"
                )
            }
        }
    }

    /**
     * Check data sync status
     */
    private fun checkSyncStatus() {
        viewModelScope.launch {
            try {
                val hasUnsyncedData = syncDataUseCase.hasUnsyncedData()
                _uiState.value = _uiState.value.copy(hasUnsyncedData = hasUnsyncedData)
            } catch (e: Exception) {
                // Ignore sync status errors
            }
        }
    }

    /**
     * Sign out user
     */
    fun signOut() {
        _uiState.value = _uiState.value.copy(isSigningOut = true)

        viewModelScope.launch {
            try {
                println("DEBUG: ProfileViewModel - Starting sign out process")

                // Call repository sign out (which handles both Firebase and DataStore)
                authRepository.signOut()

                println("DEBUG: ProfileViewModel - Sign out completed")

                _uiState.value = _uiState.value.copy(
                    isSigningOut = false,
                    signOutCompleted = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                println("DEBUG: ProfileViewModel - Sign out error: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isSigningOut = false,
                    errorMessage = e.message ?: "Failed to sign out"
                )
            }
        }
    }

    /**
     * Sync data to cloud
     */
    fun syncData() {
        val currentUid = userUid.value
        if (currentUid.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "User not logged in"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isSyncing = true)

        viewModelScope.launch {
            val result = syncDataUseCase(currentUid)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        hasUnsyncedData = false,
                        syncCompleted = true,
                        errorMessage = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        errorMessage = exception.message ?: "Sync failed"
                    )
                }
            )
        }
    }

    /**
     * Update user profile information
     */
    fun updateProfile(name: String, height: Float, weight: Float) {
        _uiState.value = _uiState.value.copy(isUpdatingProfile = true)

        viewModelScope.launch {
            try {
                // TODO: Implement profile update logic
                // This would typically update Firebase user profile and local preferences

                _uiState.value = _uiState.value.copy(
                    isUpdatingProfile = false,
                    profileUpdated = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdatingProfile = false,
                    errorMessage = e.message ?: "Failed to update profile"
                )
            }
        }
    }

    /**
     * Export user data
     */
    fun exportData() {
        val currentUid = userUid.value
        if (currentUid.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "User not logged in"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isExportingData = true)

        viewModelScope.launch {
            try {
                // TODO: Implement data export logic
                // This would generate CSV/JSON files with user's workout data

                _uiState.value = _uiState.value.copy(
                    isExportingData = false,
                    dataExported = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExportingData = false,
                    errorMessage = e.message ?: "Failed to export data"
                )
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Clear one-time events
     */
    fun clearEvents() {
        _uiState.value = _uiState.value.copy(
            signOutCompleted = false,
            syncCompleted = false,
            profileUpdated = false,
            dataExported = false
        )
    }

    /**
     * Refresh profile data
     */
    fun refreshProfile() {
        loadProfileData()
        checkSyncStatus()
    }

    /**
     * Show sign out confirmation dialog
     */
    fun showSignOutDialog() {
        _uiState.value = _uiState.value.copy(showSignOutDialog = true)
    }

    /**
     * Hide sign out confirmation dialog
     */
    fun hideSignOutDialog() {
        _uiState.value = _uiState.value.copy(showSignOutDialog = false)
    }

    /**
     * Get display name for user
     */
    fun getDisplayName(): String {
        val name = userName.value
        val email = userEmail.value

        return when {
            name.isNotEmpty() -> name
            email.isNotEmpty() -> email.substringBefore('@')
            else -> "User"
        }
    }

    /**
     * Get user statistics summary
     */
    fun getStatisticsSummary(): UserStatisticsSummary {
        val stats = workoutStatistics.value
        val profile = userProfile.value

        return UserStatisticsSummary(
            totalWorkouts = stats?.totalWorkouts ?: 0,
            currentStreak = stats?.currentStreak ?: 0,
            totalVolumeLifted = stats?.totalVolumeLifted ?: 0f,
            averageWorkoutDuration = stats?.averageWorkoutDuration ?: 0L,
            totalPersonalRecords = stats?.totalPersonalRecords ?: 0,
            memberSince = profile.joinDate
        )
    }

    /**
     * Calculate goal progress percentage
     */
    fun getGoalProgress(): Float {
        val profile = userProfile.value
        val currentWeight = profile.currentWeight.replace("kg", "").trim().toFloatOrNull() ?: 75f
        val targetWeight = profile.targetWeight.replace("kg", "").trim().toFloatOrNull() ?: 70f
        val initialWeight = 80f // TODO: Get from user's initial weight

        if (initialWeight == targetWeight) return 100f

        val totalWeightToLose = initialWeight - targetWeight
        val weightLost = initialWeight - currentWeight

        return ((weightLost / totalWeightToLose) * 100f).coerceIn(0f, 100f)
    }
}

/**
 * UI State for Profile screen
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSigningOut: Boolean = false,
    val isSyncing: Boolean = false,
    val isUpdatingProfile: Boolean = false,
    val isExportingData: Boolean = false,
    val errorMessage: String? = null,
    val hasUnsyncedData: Boolean = false,
    val showSignOutDialog: Boolean = false,

    // One-time events
    val signOutCompleted: Boolean = false,
    val syncCompleted: Boolean = false,
    val profileUpdated: Boolean = false,
    val dataExported: Boolean = false
) {
    val isAnyOperationInProgress: Boolean
        get() = isLoading || isSigningOut || isSyncing || isUpdatingProfile || isExportingData
}

/**
 * User profile data
 */
data class UserProfileData(
    val name: String = "",
    val email: String = "",
    val uid: String = "",
    val joinDate: String = "",
    val currentWeight: String = "0 kg",
    val targetWeight: String = "0 kg",
    val height: String = "0 cm",
    val totalWorkouts: Int = 0,
    val currentStreak: Int = 0,
    val totalVolumeLifted: Float = 0f
)

/**
 * User statistics summary
 */
data class UserStatisticsSummary(
    val totalWorkouts: Int,
    val currentStreak: Int,
    val totalVolumeLifted: Float,
    val averageWorkoutDuration: Long,
    val totalPersonalRecords: Int,
    val memberSince: String
)