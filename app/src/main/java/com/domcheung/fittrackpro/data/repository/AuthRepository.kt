package com.domcheung.fittrackpro.data.repository

import com.domcheung.fittrackpro.data.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun registerUser(
        email: String,
        password: String,
        user: User
    ): Flow<Result<FirebaseUser?>>
}