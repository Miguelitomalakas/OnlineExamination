package com.onlineexamination.data.repository

import com.onlineexamination.data.model.User
import com.onlineexamination.data.model.StudentInfo
import com.onlineexamination.data.model.TeacherInfo
import com.onlineexamination.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signUp(
        email: String,
        password: String,
        username: String,
        role: UserRole,
        studentInfo: StudentInfo? = null,
        teacherInfo: TeacherInfo? = null
    ): Result<FirebaseUser> {
        return try {
            // Clean and validate email
            val cleanedEmail = email.trim().lowercase()
            if (cleanedEmail.isEmpty()) {
                return Result.failure(Exception("Email cannot be empty"))
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanedEmail).matches()) {
                return Result.failure(Exception("Please enter a valid email address (e.g., user@example.com)"))
            }
            
            var user: FirebaseUser? = null
            
            // Try to create user in Firebase Auth
            try {
                val result = auth.createUserWithEmailAndPassword(cleanedEmail, password).await()
                user = result.user ?: return Result.failure(Exception("User creation failed"))
            } catch (e: Exception) {
                // If email already exists, try to sign in and check if Firestore document exists
                if (e.message?.contains("already in use", ignoreCase = true) == true ||
                    e.message?.contains("email-already-in-use", ignoreCase = true) == true) {
                    
                    // Try to sign in with the email and password
                    try {
                        val signInResult = auth.signInWithEmailAndPassword(cleanedEmail, password).await()
                        user = signInResult.user
                        
                        if (user != null) {
                            // Check if user document exists in Firestore
                            val userDoc = firestore.collection("users").document(user.uid).get().await()
                            
                            if (userDoc.exists()) {
                                // User already exists in both Auth and Firestore
                                auth.signOut() // Sign out since we signed in just to check
                                return Result.failure(Exception("An account with this email already exists. Please sign in instead."))
                            }
                            
                            // User exists in Auth but not in Firestore (orphaned user)
                            // Continue with completing the signup process below
                        }
                    } catch (signInError: Exception) {
                        // Wrong password or other sign-in error
                        return Result.failure(Exception("An account with this email already exists. If this is your account, please sign in instead."))
                    }
                } else {
                    // Other error during user creation
                    return Result.failure(e)
                }
            }
            
            if (user == null) {
                return Result.failure(Exception("User creation failed"))
            }
            
            // Now check LRN uniqueness using a separate index collection (no index needed)
            if (role == UserRole.STUDENT && studentInfo != null && studentInfo.lrn.isNotBlank()) {
                val lrn = studentInfo.lrn.trim()
                // Check if LRN already exists in the index collection
                val lrnDoc = firestore.collection("lrn_index").document(lrn).get().await()
                if (lrnDoc.exists()) {
                    // LRN already exists
                    // Only delete if we just created the user (not if it was an orphaned user)
                    if (auth.currentUser?.uid == user.uid) {
                        try {
                            user.delete().await()
                        } catch (deleteError: Exception) {
                            // If delete fails, sign out instead
                            auth.signOut()
                        }
                    } else {
                        auth.signOut()
                    }
                    return Result.failure(Exception("Learner Reference Number already exists."))
                }
            }
            
            // Send email verification (only if user is not already verified)
            if (!user.isEmailVerified) {
                try {
                    user.sendEmailVerification().await()
                } catch (e: Exception) {
                    // If verification email fails, continue anyway
                }
            }
            
            // Check if user document already exists (to avoid overwriting)
            val existingUserDoc = firestore.collection("users").document(user.uid).get().await()
            
            if (!existingUserDoc.exists()) {
                // Save user data to Firestore
                val userData = User(
                    uid = user.uid,
                    email = cleanedEmail,
                    username = username,
                    role = role,
                    photoUrl = user.photoUrl?.toString() ?: "",
                    emailVerified = user.isEmailVerified
                )
                firestore.collection("users").document(user.uid).set(userData).await()
            } else {
                // User document exists, check if profile exists
                val existingProfile = when (role) {
                    UserRole.STUDENT -> {
                        firestore.collection("users").document(user.uid)
                            .collection("profiles").document("student").get().await()
                    }
                    UserRole.TEACHER -> {
                        firestore.collection("users").document(user.uid)
                            .collection("profiles").document("teacher").get().await()
                    }
                    else -> null
                }
                
                if (existingProfile?.exists() == true) {
                    // Profile already exists, account is complete
                    return Result.failure(Exception("An account with this email already exists. Please sign in instead."))
                }
            }

            // If student, persist student profile under subcollection or document
            if (role == UserRole.STUDENT && studentInfo != null) {
                val profileDoc = firestore.collection("users").document(user.uid)
                    .collection("profiles").document("student").get().await()
                
                if (!profileDoc.exists()) {
                    val lrn = studentInfo.lrn.trim()
                    
                    // Save student profile
                    firestore.collection("users").document(user.uid)
                        .collection("profiles").document("student")
                        .set(studentInfo)
                        .await()
                    
                    // Add LRN to index collection for uniqueness checking
                    if (lrn.isNotBlank()) {
                        firestore.collection("lrn_index").document(lrn)
                            .set(mapOf(
                                "userId" to user.uid,
                                "createdAt" to com.google.firebase.Timestamp.now()
                            ))
                            .await()
                    }
                }
            }

            if (role == UserRole.TEACHER && teacherInfo != null) {
                val profileDoc = firestore.collection("users").document(user.uid)
                    .collection("profiles").document("teacher").get().await()
                
                if (!profileDoc.exists()) {
                    firestore.collection("users").document(user.uid)
                        .collection("profiles").document("teacher")
                        .set(teacherInfo)
                        .await()
                }
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            // Clean and validate email
            val cleanedEmail = email.trim().lowercase()
            if (cleanedEmail.isEmpty()) {
                return Result.failure(Exception("Email cannot be empty"))
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanedEmail).matches()) {
                return Result.failure(Exception("Please enter a valid email address (e.g., user@example.com)"))
            }
            val result = auth.signInWithEmailAndPassword(cleanedEmail, password).await()
            val user = result.user ?: return Result.failure(Exception("Sign in failed"))
            getAndSaveFcmToken(user.uid)
            Result.success(user)
        } catch (e: Exception) {
            // Provide user-friendly error messages
            val errorMessage = when {
                e.message?.contains("badly formatted", ignoreCase = true) == true -> 
                    "Invalid email format. Please check your email address."
                e.message?.contains("user not found", ignoreCase = true) == true -> 
                    "No account found with this email address."
                e.message?.contains("wrong password", ignoreCase = true) == true -> 
                    "Incorrect password. Please try again."
                e.message?.contains("network", ignoreCase = true) == true -> 
                    "Network error. Please check your internet connection."
                else -> e.message ?: "Sign in failed. Please try again."
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            // Clean and validate email
            val cleanedEmail = email.trim().lowercase()
            if (cleanedEmail.isEmpty()) {
                return Result.failure(Exception("Email cannot be empty"))
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanedEmail).matches()) {
                return Result.failure(Exception("Please enter a valid email address (e.g., user@example.com)"))
            }
            auth.sendPasswordResetEmail(cleanedEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserData(uid: String): Result<User> {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            val user = document.toObject(User::class.java)
                ?: return Result.failure(Exception("User data not found"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reloadUser(): Result<FirebaseUser> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
            user.reload().await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getAndSaveFcmToken(uid: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            firestore.collection("users").document(uid).update("fcmToken", token).await()
        } catch (e: Exception) {
            // Handle exceptions, e.g., log the error
        }
    }

    fun signOut() {
        auth.signOut()
    }
}

