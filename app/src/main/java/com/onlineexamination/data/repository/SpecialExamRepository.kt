package com.onlineexamination.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.onlineexamination.data.model.Exam
import com.onlineexamination.data.model.SpecialExamRequest
import kotlinx.coroutines.tasks.await

class SpecialExamRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val requestsCollection = firestore.collection("special_exam_requests")
    private val examsCollection = firestore.collection("exams")

    suspend fun submitRequest(request: SpecialExamRequest): Result<String> {
        return try {
            // Fetch exam to get teacherId
            val examSnapshot = examsCollection.document(request.examId).get().await()
            val exam = examSnapshot.toObject(Exam::class.java)
            
            val teacherId = exam?.teacherId ?: return Result.failure(Exception("Exam not found"))

            val newRequest = request.copy(
                teacherId = teacherId,
                id = requestsCollection.document().id
            )

            requestsCollection.document(newRequest.id).set(newRequest).await()
            Result.success(newRequest.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
