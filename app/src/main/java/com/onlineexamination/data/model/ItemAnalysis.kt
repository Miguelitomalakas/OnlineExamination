package com.onlineexamination.data.model

import com.onlineexamination.data.model.Question

/**
 * Represents the analysis of a single question in an exam.
 */
data class QuestionAnalysis(
    val question: Question,
    val totalAttempts: Int,
    val correctAttempts: Int,
    val incorrectAttempts: Int,
    // The percentage of students who answered this question correctly.
    val correctPercentage: Double,
    // A map where the key is the option (e.g., "A", "B", "C", "D") and the value is the number of students who chose that option.
    val responseDistribution: Map<String, Int>
)

/**
 * Represents the overall item analysis for an exam.
 */
data class ItemAnalysis(
    val examId: String,
    val totalSubmissions: Int,
    val questionAnalyses: List<QuestionAnalysis>
)
