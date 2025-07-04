package com.domcheung.fittrackpro.data.repository

import com.domcheung.fittrackpro.data.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    // Existing register method
    fun registerUser(
        email: String,
        password: String,
        user: User
    ): Flow<Result<FirebaseUser?>>

    // New login method
    fun loginUser(
        email: String,
        password: String
    ): Flow<Result<FirebaseUser?>>

    // Check if user is currently logged in
    fun getCurrentUser(): FirebaseUser?

    // Sign out user
    fun signOut()
}