package com.onlineexamination.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.onlineexamination.data.model.StudentInfo
import com.onlineexamination.data.model.TeacherInfo
import com.onlineexamination.data.model.User
import com.onlineexamination.data.model.UserRole
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signUp(
        email: String,
        pass: String,
        username: String,
        role: UserRole,
        studentInfo: StudentInfo?,
        teacherInfo: TeacherInfo?
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user!!
            val newUser = User(user.uid, username, email, role)
            firestore.collection("users").document(user.uid).set(newUser).await()

            when (role) {
                UserRole.STUDENT -> {
                    studentInfo?.let {
                        firestore.collection("users").document(user.uid).collection("profiles").document("student").set(it).await()
                    }
                }
                UserRole.TEACHER -> {
                    teacherInfo?.let {
                        firestore.collection("users").document(user.uid).collection("profiles").document("teacher").set(it).await()
                    }
                }
                else -> {}
            }
            user.sendEmailVerification().await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user!!
            if (!user.isEmailVerified) {
                throw Exception("Please verify your email before signing in.")
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserData(uid: String): Result<User> {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            val user = document.toObject(User::class.java)
            Result.success(user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reloadUser(): Result<FirebaseUser> {
        return try {
            val user = auth.currentUser!!
            user.reload().await()
            Result.success(auth.currentUser!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
