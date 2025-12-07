package com.onlineexamination.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.onlineexamination.data.model.User
import com.onlineexamination.data.model.UserRole
import kotlinx.coroutines.tasks.await

class LeaderboardRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun getLeaderboard(): Result<List<User>> {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("role", "STUDENT")
                .orderBy("totalScore", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
