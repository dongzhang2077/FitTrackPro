package com.domcheung.fittrackpro.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension to create DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesManager @Inject constructor(
    private val context: Context
) {
    // Preference keys
    private object PreferenceKeys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_UID = stringPreferencesKey("user_uid")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_AVATAR_URL = stringPreferencesKey("user_avatar_url")
        val USER_CURRENT_WEIGHT = stringPreferencesKey("user_current_weight")
        val USER_TARGET_WEIGHT = stringPreferencesKey("user_target_weight")
        val USER_HEIGHT = stringPreferencesKey("user_height")
        val USER_INITIAL_WEIGHT = stringPreferencesKey("user_initial_weight")
        val WEEKLY_WORKOUT_GOAL = intPreferencesKey("weekly_workout_goal")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    // Save login state
    suspend fun saveLoginState(
        isLoggedIn: Boolean,
        userEmail: String = "",
        userUid: String = "",
        userName: String = ""
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.IS_LOGGED_IN] = isLoggedIn
            preferences[PreferenceKeys.USER_EMAIL] = userEmail
            preferences[PreferenceKeys.USER_UID] = userUid
            preferences[PreferenceKeys.USER_NAME] = userName
        }
    }

    // Get login state
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.IS_LOGGED_IN] ?: false
    }

    // Get user email
    val userEmail: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USER_EMAIL] ?: ""
    }

    // Get user UID
    val userUid: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USER_UID] ?: ""
    }

    // Get user name
    val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USER_NAME] ?: ""
    }

    // Get user avatar URL
    val userAvatarUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USER_AVATAR_URL] ?: ""
    }

    // Get user current weight
    val userCurrentWeight: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USER_CURRENT_WEIGHT] ?: ""
    }

    // Get user target weight
    val userTargetWeight: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USER_TARGET_WEIGHT] ?: ""
    }

    // Get user height
    val userHeight: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USER_HEIGHT] ?: ""
    }

    // Get user initial weight
    val userInitialWeight: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USER_INITIAL_WEIGHT] ?: ""
    }

    // Save user profile data
    suspend fun saveUserProfile(
        name: String? = null,
        avatarUrl: String = "",
        currentWeight: String = "",
        targetWeight: String = "",
        height: String = "",
        initialWeight: String = ""
    ) {
        context.dataStore.edit { preferences ->
            // Only update name if it's provided (not null)
            name?.let {
                preferences[PreferenceKeys.USER_NAME] = it
            }
            preferences[PreferenceKeys.USER_AVATAR_URL] = avatarUrl
            preferences[PreferenceKeys.USER_CURRENT_WEIGHT] = currentWeight
            preferences[PreferenceKeys.USER_TARGET_WEIGHT] = targetWeight
            preferences[PreferenceKeys.USER_HEIGHT] = height
            preferences[PreferenceKeys.USER_INITIAL_WEIGHT] = initialWeight
        }
    }

    // Update user avatar URL
    suspend fun updateUserAvatar(avatarUrl: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.USER_AVATAR_URL] = avatarUrl
        }
    }

    // Update user weight goals
    suspend fun updateWeightGoals(
        currentWeight: String,
        targetWeight: String,
        initialWeight: String = ""
    ) {
        context.dataStore.edit { preferences ->
            if (currentWeight.isNotBlank()) {
                preferences[PreferenceKeys.USER_CURRENT_WEIGHT] = currentWeight
            }
            if (targetWeight.isNotBlank()) {
                preferences[PreferenceKeys.USER_TARGET_WEIGHT] = targetWeight
            }
            if (initialWeight.isNotBlank()) {
                preferences[PreferenceKeys.USER_INITIAL_WEIGHT] = initialWeight
            }
        }
    }

    // Get onboarding completion status
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.ONBOARDING_COMPLETED] ?: false
    }

    // Mark onboarding as completed
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    // Clear all user data (for logout)
    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Get user weekly workout goal
    val weeklyWorkoutGoal: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.WEEKLY_WORKOUT_GOAL] ?: 3 // Default to 3 workouts per week
    }
    
    // Save user weekly workout goal
    suspend fun saveWeeklyWorkoutGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.WEEKLY_WORKOUT_GOAL] = goal
        }
    }
    
    // Check if user data exists - simplified version
    suspend fun hasUserData(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[PreferenceKeys.IS_LOGGED_IN] ?: false
        }.first() // Use first() to get single value instead of collect
    }
}
