package com.domcheung.fittrackpro.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

    // Clear all user data (for logout)
    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Check if user data exists - simplified version
    suspend fun hasUserData(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[PreferenceKeys.IS_LOGGED_IN] ?: false
        }.first() // Use first() to get single value instead of collect
    }
}