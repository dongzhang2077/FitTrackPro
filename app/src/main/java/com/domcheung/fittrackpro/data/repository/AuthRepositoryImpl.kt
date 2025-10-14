package com.domcheung.fittrackpro.data.repository

import com.domcheung.fittrackpro.data.local.UserPreferencesManager
import com.domcheung.fittrackpro.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userPreferencesManager: UserPreferencesManager
) : AuthRepository {

    // Existing register method
    override fun registerUser(email: String, password: String, user: User): Flow<Result<FirebaseUser?>> = callbackFlow {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val uid = firebaseUser?.uid ?: ""
                    val userWithUid = user.copy(uid = uid)

                    firestore.collection("users").document(uid)
                        .set(userWithUid)
                        .addOnSuccessListener {
                            // Save login state after successful registration
                            CoroutineScope(Dispatchers.IO).launch {
                                // Save with the user's actual name from registration form
                                saveLoginStateWithName(firebaseUser!!, user.name)
                                println("DEBUG: Registration success - saved login state for user: ${user.name} (${firebaseUser.email})")
                            }
                            trySend(Result.success(firebaseUser))
                        }
                        .addOnFailureListener {
                            trySend(Result.failure(it))
                        }
                } else {
                    trySend(Result.failure(task.exception ?: Exception("Unknown error")))
                }
            }

        awaitClose { }
    }

    // Updated login method with state persistence
    override fun loginUser(email: String, password: String): Flow<Result<FirebaseUser?>> = callbackFlow {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        // Save login state to DataStore
                        CoroutineScope(Dispatchers.IO).launch {
                            saveLoginState(firebaseUser)
                        }
                    }
                    trySend(Result.success(firebaseUser))
                } else {
                    trySend(Result.failure(task.exception ?: Exception("Login failed")))
                }
            }

        awaitClose { }
    }

    // Get current logged in user
    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Updated sign out with state clearing
    override fun signOut() {
        println("DEBUG: Starting sign out...")

        // First clear Firebase Auth
        auth.signOut()
        println("DEBUG: Firebase auth signed out, current user: ${auth.currentUser}")

        // Then clear DataStore state synchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                clearLoginState()
                println("DEBUG: DataStore clear completed")
            } catch (e: Exception) {
                println("DEBUG: Error clearing DataStore: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // New login state management methods
    override fun isLoggedIn(): Flow<Boolean> {
        return userPreferencesManager.isLoggedIn
    }

    override suspend fun saveLoginState(user: FirebaseUser) {
        userPreferencesManager.saveLoginState(
            isLoggedIn = true,
            userEmail = user.email ?: "",
            userUid = user.uid,
            userName = user.displayName ?: ""
        )
    }

    // New method to save login state with custom name
    override suspend fun saveLoginStateWithName(user: FirebaseUser, name: String) {
        userPreferencesManager.saveLoginState(
            isLoggedIn = true,
            userEmail = user.email ?: "",
            userUid = user.uid,
            userName = name
        )
    }

    override suspend fun clearLoginState() {
        userPreferencesManager.clearUserData()
    }

    override suspend fun checkInitialLoginState(): Boolean {
        // Check Firebase Auth first
        val firebaseUser = getCurrentUser()
        if (firebaseUser == null) {
            return false
        }

        // Then check DataStore
        val hasStoredData = userPreferencesManager.hasUserData()

        // Both must be true for user to be considered logged in
        return hasStoredData
    }
}