package com.onlineexamination.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class SpecialExamRequest(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val examId: String = "",
    val examTitle: String = "",
    val reason: String = "",
    val fileUrl: String = "",
    val status: String = "Pending", // Pending, Approved, Rejected
    @ServerTimestamp
    val requestedAt: Date? = null
)
