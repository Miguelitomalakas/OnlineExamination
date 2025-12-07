package com.onlineexamination.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FileRepository {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    suspend fun uploadImage(imageUri: Uri, folder: String): Result<String> {
        return try {
            val filename = "${UUID.randomUUID()}.jpg"
            val fileRef = storageRef.child("$folder/$filename")
            
            fileRef.putFile(imageUri).await()
            val downloadUrl = fileRef.downloadUrl.await()
            
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadFile(fileUri: Uri, folder: String, originalName: String? = null): Result<String> {
        return try {
            val name = originalName ?: "${UUID.randomUUID()}"
            val fileRef = storageRef.child("$folder/$name")
            
            fileRef.putFile(fileUri).await()
            val downloadUrl = fileRef.downloadUrl.await()
            
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
