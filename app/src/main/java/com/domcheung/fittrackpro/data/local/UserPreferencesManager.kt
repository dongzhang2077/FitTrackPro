package com.domcheung.fittrackpro.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.domcheung.fittrackpro.data.reminder.ReminderSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
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
        val FITNESS_PRIMARY_GOAL = stringPreferencesKey("fitness_primary_goal")
        val FITNESS_WORKOUT_FREQUENCY = intPreferencesKey("fitness_workout_frequency")
        val FITNESS_EXPERIENCE_LEVEL = stringPreferencesKey("fitness_experience_level")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val REMINDER_DAYS = stringSetPreferencesKey("reminder_days")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val THEME_DYNAMIC_COLOR = booleanPreferencesKey("theme_dynamic_color")
    }

    private companion object {
        const val DEFAULT_FITNESS_GOAL = "GENERAL_FITNESS"
        const val DEFAULT_EXPERIENCE_LEVEL = "BEGINNER"
        const val DEFAULT_WORKOUT_FREQUENCY = 3
        const val DEFAULT_THEME_MODE = "SYSTEM"
    }

    // Save login state
    suspend fun saveLoginState(
        isLoggedIn: Boolean,
        userEmail: String = "",
        userUid: String = "",
        userName: String = ""
    ) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.IS_LOGGED_IN] = isLoggedIn
            preferences[PreferenceKeys.USER_EMAIL] = userEmail
            preferences[PreferenceKeys.USER_UID] = userUid
            preferences[PreferenceKeys.USER_NAME] = userName
        }
    }

    // Get login state
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.IS_LOGGED_IN] ?: false
    }

    // Get user email
    val userEmail: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USER_EMAIL] ?: ""
    }

    // Get user UID
    val userUid: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USER_UID] ?: ""
    }

    // Get user name
    val userName: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USER_NAME] ?: ""
    }

    // Get user avatar URL
    val userAvatarUrl: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USER_AVATAR_URL] ?: ""
    }

    // Get user current weight
    val userCurrentWeight: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USER_CURRENT_WEIGHT] ?: ""
    }

    // Get user target weight
    val userTargetWeight: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USER_TARGET_WEIGHT] ?: ""
    }

    // Get user height
    val userHeight: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USER_HEIGHT] ?: ""
    }

    // Get user initial weight
    val userInitialWeight: Flow<String> = dataStore.data.map { preferences ->
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
        dataStore.edit { preferences ->
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
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.USER_AVATAR_URL] = avatarUrl
        }
    }

    // Update user weight goals
    suspend fun updateWeightGoals(
        currentWeight: String,
        targetWeight: String,
        initialWeight: String = ""
    ) {
        dataStore.edit { preferences ->
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
    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.ONBOARDING_COMPLETED] ?: false
    }

    // Mark onboarding as completed
    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    // Clear all user data (for logout)
    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Get user weekly workout goal
    val weeklyWorkoutGoal: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.WEEKLY_WORKOUT_GOAL] ?: 3 // Default to 3 workouts per week
    }
    
    // Save user weekly workout goal
    suspend fun saveWeeklyWorkoutGoal(goal: Int) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.WEEKLY_WORKOUT_GOAL] = goal
        }
    }

    val fitnessPrimaryGoal: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.FITNESS_PRIMARY_GOAL]
            ?.trim()
            ?.ifBlank { DEFAULT_FITNESS_GOAL }
            ?: DEFAULT_FITNESS_GOAL
    }

    val fitnessWorkoutFrequency: Flow<Int> = dataStore.data.map { preferences ->
        (preferences[PreferenceKeys.FITNESS_WORKOUT_FREQUENCY] ?: DEFAULT_WORKOUT_FREQUENCY)
            .coerceIn(1, 7)
    }

    val fitnessExperienceLevel: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.FITNESS_EXPERIENCE_LEVEL]
            ?.trim()
            ?.ifBlank { DEFAULT_EXPERIENCE_LEVEL }
            ?: DEFAULT_EXPERIENCE_LEVEL
    }

    suspend fun saveFitnessPreferences(
        primaryGoal: String,
        workoutFrequencyPerWeek: Int,
        experienceLevel: String
    ) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.FITNESS_PRIMARY_GOAL] =
                primaryGoal.trim().ifBlank { DEFAULT_FITNESS_GOAL }
            preferences[PreferenceKeys.FITNESS_WORKOUT_FREQUENCY] =
                workoutFrequencyPerWeek.coerceIn(1, 7)
            preferences[PreferenceKeys.FITNESS_EXPERIENCE_LEVEL] =
                experienceLevel.trim().ifBlank { DEFAULT_EXPERIENCE_LEVEL }
        }
    }

    val reminderEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.REMINDER_ENABLED] ?: false
    }

    val reminderHour: Flow<Int> = dataStore.data.map { preferences ->
        (preferences[PreferenceKeys.REMINDER_HOUR] ?: 19).coerceIn(0, 23)
    }

    val reminderMinute: Flow<Int> = dataStore.data.map { preferences ->
        (preferences[PreferenceKeys.REMINDER_MINUTE] ?: 0).coerceIn(0, 59)
    }

    val reminderSelectedDays: Flow<Set<Int>> = dataStore.data.map { preferences ->
        val saved = preferences[PreferenceKeys.REMINDER_DAYS]
        parseReminderDays(saved)
    }

    val reminderSettings: Flow<ReminderSettings> = combine(
        reminderEnabled,
        reminderHour,
        reminderMinute,
        reminderSelectedDays
    ) { enabled, hour, minute, days ->
        ReminderSettings(
            enabled = enabled,
            hour = hour,
            minute = minute,
            selectedDays = days
        )
    }

    val themeMode: Flow<AppThemeMode> = dataStore.data.map { preferences ->
        AppThemeMode.fromRaw(preferences[PreferenceKeys.THEME_MODE] ?: DEFAULT_THEME_MODE)
    }

    val dynamicColorEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.THEME_DYNAMIC_COLOR] ?: false
    }

    val themeSettings: Flow<AppThemeSettings> = combine(
        themeMode,
        dynamicColorEnabled
    ) { mode, dynamicColor ->
        AppThemeSettings(
            mode = mode,
            dynamicColorEnabled = dynamicColor
        )
    }

    suspend fun updateThemeMode(mode: AppThemeMode) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun updateDynamicColorEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME_DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun updateThemeSettings(settings: AppThemeSettings) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME_MODE] = settings.mode.name
            preferences[PreferenceKeys.THEME_DYNAMIC_COLOR] = settings.dynamicColorEnabled
        }
    }

    suspend fun updateReminderEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.REMINDER_ENABLED] = enabled
        }
    }

    suspend fun updateReminderTime(hour: Int, minute: Int) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.REMINDER_HOUR] = hour.coerceIn(0, 23)
            preferences[PreferenceKeys.REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun updateReminderSelectedDays(days: Set<Int>) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.REMINDER_DAYS] = serializeReminderDays(days)
        }
    }

    suspend fun updateReminderSettings(settings: ReminderSettings) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.REMINDER_ENABLED] = settings.enabled
            preferences[PreferenceKeys.REMINDER_HOUR] = settings.hour.coerceIn(0, 23)
            preferences[PreferenceKeys.REMINDER_MINUTE] = settings.minute.coerceIn(0, 59)
            preferences[PreferenceKeys.REMINDER_DAYS] = serializeReminderDays(settings.selectedDays)
        }
    }
    
    // Check if user data exists - simplified version
    suspend fun hasUserData(): Boolean {
        return dataStore.data.map { preferences ->
            preferences[PreferenceKeys.IS_LOGGED_IN] ?: false
        }.first() // Use first() to get single value instead of collect
    }

    private fun parseReminderDays(rawDays: Set<String>?): Set<Int> {
        val defaultDays = setOf(1, 2, 3, 4, 5, 6, 7)
        if (rawDays.isNullOrEmpty()) {
            return defaultDays
        }

        val parsed = rawDays.mapNotNull { value ->
            value.toIntOrNull()?.takeIf { day -> day in 1..7 }
        }.toSet()

        return if (parsed.isEmpty()) defaultDays else parsed
    }

    private fun serializeReminderDays(days: Set<Int>): Set<String> {
        val normalized = days.filter { day -> day in 1..7 }.toSet()
        val safeDays = if (normalized.isEmpty()) setOf(1, 2, 3, 4, 5, 6, 7) else normalized
        return safeDays.map { day -> day.toString() }.toSet()
    }
}
