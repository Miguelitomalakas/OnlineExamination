package com.onlineexamination.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.onlineexamination.data.model.SpecialExamRequest
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SpecialExamRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val specialExamRequestsCollection = firestore.collection("special_exam_requests")

    suspend fun createSpecialExamRequest(request: SpecialExamRequest, fileUri: Uri): Result<Unit> {
        return try {
            val fileName = "special_exam_requests/${request.studentId}/${UUID.randomUUID()}"
            val uploadTask = storage.reference.child(fileName).putFile(fileUri).await()
            val fileUrl = uploadTask.storage.downloadUrl.await().toString()

            val requestWithFileUrl = request.copy(fileUrl = fileUrl)
            specialExamRequestsCollection.add(requestWithFileUrl).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
