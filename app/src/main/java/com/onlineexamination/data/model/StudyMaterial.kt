package com.onlineexamination.data.model

data class StudyMaterial(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val subject: String = "",
    val gradeLevel: String = "",
    val resourceType: String = "Document",
    val downloadUrl: String = "",
    val uploadedBy: String = "",
    val uploadedByName: String = "",
    val uploadedAt: Long = System.currentTimeMillis()
)


