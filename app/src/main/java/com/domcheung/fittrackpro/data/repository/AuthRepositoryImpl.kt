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
}