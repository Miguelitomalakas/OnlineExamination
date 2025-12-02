package com.onlineexamination.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.onlineexamination.data.model.ExamAttempt
import com.onlineexamination.data.model.Leaderboard
import com.onlineexamination.data.model.LeaderboardEntry
import kotlinx.coroutines.tasks.await

class LeaderboardRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getLeaderboard(term: String): Result<Leaderboard> {
        return try {
            val attempts = firestore.collectionGroup("exam_attempts")
                .whereEqualTo("term", term)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()

            val entries = attempts.documents.mapNotNull { doc ->
                val attempt = doc.toObject(ExamAttempt::class.java)
                if (attempt != null) {
                    LeaderboardEntry(
                        studentId = attempt.studentId,
                        studentName = attempt.studentName,
                        score = attempt.score
                    )
                } else {
                    null
                }
            }.groupBy { it.studentId }
                .map { (studentId, entries) ->
                    LeaderboardEntry(
                        studentId = studentId,
                        studentName = entries.first().studentName,
                        score = entries.sumOf { it.score }
                    )
                }
                .sortedByDescending { it.score }
                .mapIndexed { index, entry ->
                    entry.copy(rank = index + 1)
                }

            Result.success(Leaderboard(term, entries))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
