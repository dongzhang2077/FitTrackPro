package com.domcheung.fittrackpro.data.repository

import com.domcheung.fittrackpro.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    // Existing register method (keep as is)
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

    // New login method implementation
    override fun loginUser(email: String, password: String): Flow<Result<FirebaseUser?>> = callbackFlow {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
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

    // Sign out current user
    override fun signOut() {
        auth.signOut()
    }
}