package com.domcheung.fittrackpro.data.repository

import com.domcheung.fittrackpro.data.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    // Existing authentication methods
    fun registerUser(
        email: String,
        password: String,
        user: User
    ): Flow<Result<FirebaseUser?>>

    fun loginUser(
        email: String,
        password: String
    ): Flow<Result<FirebaseUser?>>

    fun getCurrentUser(): FirebaseUser?

    fun signOut()

    // New login state management methods
    fun isLoggedIn(): Flow<Boolean>

    suspend fun saveLoginState(user: FirebaseUser)

    suspend fun saveLoginStateWithName(user: FirebaseUser, name: String)

    suspend fun clearLoginState()

    suspend fun checkInitialLoginState(): Boolean
}