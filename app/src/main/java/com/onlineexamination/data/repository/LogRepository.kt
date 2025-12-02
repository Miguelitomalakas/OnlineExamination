package com.onlineexamination.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.onlineexamination.data.model.StudentLog
import kotlinx.coroutines.tasks.await

class LogRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val logsCollection = firestore.collection("student_logs")

    suspend fun addLog(log: StudentLog): Result<Unit> {
        return try {
            logsCollection.add(log).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLogsByStudent(studentId: String): Result<List<StudentLog>> {
        return try {
            val snapshot = logsCollection
                .whereEqualTo("studentId", studentId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val logs = snapshot.documents.mapNotNull { it.toObject(StudentLog::class.java) }
            Result.success(logs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
