package com.onlineexamination.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class StudentLog(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val eventType: String = "", // e.g., "Login", "Tab Change"
    val eventDetails: String = "", // e.g., "Logged in from device", "Switched to another tab"
    @ServerTimestamp
    val timestamp: Date? = null
)
