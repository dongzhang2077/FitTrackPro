package com.domcheung.fittrackpro.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domcheung.fittrackpro.data.local.UserPreferencesManager
import com.domcheung.fittrackpro.data.repository.AuthRepository
import com.domcheung.fittrackpro.domain.usecase.GetWorkoutStatisticsUseCase
import com.domcheung.fittrackpro.domain.usecase.SyncDataUseCase
import com.domcheung.fittrackpro.data.repository.WorkoutStatistics
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

    // User avatar URL
    val userAvatarUrl: StateFlow<String> = userPreferencesManager.userAvatarUrl
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    // User physical data
    val userCurrentWeight: StateFlow<String> = userPreferencesManager.userCurrentWeight
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "75 kg"
        )

    val userTargetWeight: StateFlow<String> = userPreferencesManager.userTargetWeight
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "70 kg"
        )

    val userHeight: StateFlow<String> = userPreferencesManager.userHeight
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "175 cm"
        )

    val userInitialWeight: StateFlow<String> = userPreferencesManager.userInitialWeight
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "80 kg"
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
        userAvatarUrl,
        userCurrentWeight,
        userTargetWeight,
        userHeight,
        userInitialWeight,
        workoutStatistics
    ) { values ->
        val name = (values[0] as String).trim()
        val email = values[1] as String
        val uid = values[2] as String
        val avatarUrl = values[3] as String
        val currentWeight = values[4] as String
        val targetWeight = values[5] as String
        val height = values[6] as String
        val initialWeight = values[7] as String
        val stats = values[8] as? WorkoutStatistics

        val sanitizedCurrent = sanitizeNumberString(currentWeight)
        val sanitizedTarget = sanitizeNumberString(targetWeight)
        val sanitizedHeight = sanitizeNumberString(height)
        val sanitizedInitial = sanitizeNumberString(initialWeight)

        UserProfileData(
            name = name,
            email = email,
            uid = uid,
            avatarUrl = avatarUrl,
            joinDate = "Member since Jan 2025", // TODO: Get actual join date
            currentWeight = sanitizedCurrent,
            targetWeight = sanitizedTarget,
            height = sanitizedHeight,
            initialWeight = if (sanitizedInitial.isNotEmpty()) sanitizedInitial else sanitizedCurrent,
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
        performAutoSyncIfNeeded()
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
     * Perform automatic sync if needed
     */
    private fun performAutoSyncIfNeeded() {
        viewModelScope.launch {
            try {
                val currentUid = userUid.value
                if (currentUid.isNotEmpty()) {
                    syncDataUseCase.performAutoSync(currentUid)
                    checkSyncStatus() // Update status after sync attempt
                }
            } catch (e: Exception) {
                // Silently ignore auto sync errors - don't bother the user
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
            val result = syncDataUseCase.performAutoSync(currentUid)

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
     * Update user name
     */
    fun updateUserName(newName: String) {
        val trimmedName = newName.trim()
        if (trimmedName.length < 2) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Name must be at least 2 characters"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isUpdatingProfile = true)

        viewModelScope.launch {
            try {
                val sanitizedCurrent = sanitizeNumberString(userCurrentWeight.value)
                val sanitizedTarget = sanitizeNumberString(userTargetWeight.value)
                val sanitizedHeight = sanitizeNumberString(userHeight.value)
                val sanitizedInitial = sanitizeNumberString(userInitialWeight.value)

                userPreferencesManager.saveUserProfile(
                    name = trimmedName,
                    avatarUrl = userAvatarUrl.value,
                    currentWeight = sanitizedCurrent,
                    targetWeight = sanitizedTarget,
                    height = sanitizedHeight,
                    initialWeight = sanitizedInitial
                )

                _uiState.value = _uiState.value.copy(
                    isUpdatingProfile = false,
                    profileUpdated = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdatingProfile = false,
                    errorMessage = e.message ?: "Failed to update name"
                )
            }
        }
    }

    /**
     * Update user avatar URL (supports both URL and default avatar format)
     */
    fun updateUserAvatar(avatarData: String) {
        _uiState.value = _uiState.value.copy(isUpdatingProfile = true)

        viewModelScope.launch {
            try {
                userPreferencesManager.updateUserAvatar(avatarData)

                _uiState.value = _uiState.value.copy(
                    isUpdatingProfile = false,
                    profileUpdated = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdatingProfile = false,
                    errorMessage = e.message ?: "Failed to update avatar"
                )
            }
        }
    }

    /**
     * Update user weight goals
     */
    fun updateWeightGoals(currentWeight: String, targetWeight: String, initialWeight: String = "") {
        _uiState.value = _uiState.value.copy(isUpdatingProfile = true)

        viewModelScope.launch {
            try {
                val sanitizedCurrent = sanitizeNumberString(currentWeight)
                val sanitizedTarget = sanitizeNumberString(targetWeight)
                val sanitizedInitial = sanitizeNumberString(initialWeight)

                userPreferencesManager.updateWeightGoals(
                    currentWeight = sanitizedCurrent,
                    targetWeight = sanitizedTarget,
                    initialWeight = sanitizedInitial
                )

                _uiState.value = _uiState.value.copy(
                    isUpdatingProfile = false,
                    profileUpdated = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdatingProfile = false,
                    errorMessage = e.message ?: "Failed to update weight goals"
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
        val name = userName.value.trim()
        val email = userEmail.value.trim()

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
        if (profile.currentWeight.isBlank() || profile.targetWeight.isBlank() || profile.initialWeight.isBlank()) {
            return 0f
        }

        val currentWeight = parseNumber(profile.currentWeight, fallback = 0f)
        val targetWeight = parseNumber(profile.targetWeight, fallback = currentWeight)
        val initialWeight = parseNumber(profile.initialWeight, fallback = currentWeight)

        val totalChange = targetWeight - initialWeight
        if (totalChange == 0f) {
            return if (currentWeight == initialWeight && currentWeight != 0f) 100f else 0f
        }

        val achievedChange = currentWeight - initialWeight
        val progressFraction = achievedChange / totalChange

        return (progressFraction.coerceIn(0f, 1f) * 100f)
    }

    private fun sanitizeNumberString(value: String): String {
        var decimalFound = false
        val sanitized = buildString {
            value.forEach { char ->
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

    private fun parseNumber(value: String, fallback: Float): Float {
        val sanitized = sanitizeNumberString(value)
        return sanitized.toFloatOrNull() ?: fallback
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
    val avatarUrl: String = "",
    val joinDate: String = "",
    val currentWeight: String = "",
    val targetWeight: String = "",
    val height: String = "",
    val initialWeight: String = "",
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
