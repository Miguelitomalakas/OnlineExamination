package com.onlineexamination.data.model

data class SpecialExamRequest(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val examId: String = "",
    val examTitle: String = "",
    val teacherId: String = "",
    val reason: String = "", // "Medical", "Death in Family", "Other"
    val description: String = "",
    val proofFileUrl: String = "", // URL to the uploaded certificate
    val status: RequestStatus = RequestStatus.PENDING,
    val requestedAt: Long = System.currentTimeMillis()
)

enum class RequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}
