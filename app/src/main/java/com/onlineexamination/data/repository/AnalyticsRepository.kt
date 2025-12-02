package com.onlineexamination.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.onlineexamination.data.model.Exam
import com.onlineexamination.data.model.ExamAttempt
import com.onlineexamination.data.model.ItemAnalysis
import com.onlineexamination.data.model.QuestionAnalysis
import kotlinx.coroutines.tasks.await

class AnalyticsRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val examsCollection = firestore.collection("exams")
    private val attemptsCollection = firestore.collection("exam_attempts")

    /**
     * Generates an item analysis for a given exam.
     *
     * @param examId The ID of the exam to analyze.
     * @return A [Result] containing the [ItemAnalysis] or an error.
     */
    suspend fun getItemAnalysis(examId: String): Result<ItemAnalysis> {
        return try {
            // Get the exam details
            val examSnapshot = examsCollection.document(examId).get().await()
            val exam = examSnapshot.toObject(Exam::class.java)
                ?: return Result.failure(Exception("Exam not found"))

            // Get all attempts for the exam
            val attemptsSnapshot = attemptsCollection.whereEqualTo("examId", examId).get().await()
            val attempts = attemptsSnapshot.documents.mapNotNull { it.toObject(ExamAttempt::class.java) }

            if (attempts.isEmpty()) {
                return Result.success(ItemAnalysis(examId, 0, emptyList()))
            }

            val totalSubmissions = attempts.size
            val questionAnalyses = exam.questions.map { question ->
                val correctAttempts = attempts.count { attempt ->
                    val studentAnswer = attempt.answers[question.id]
                    studentAnswer != null && studentAnswer.equals(question.correctAnswer, ignoreCase = true)
                }

                val responseDistribution = question.options.associateWith { option ->
                    attempts.count { attempt ->
                        attempt.answers[question.id] == option
                    }
                }

                QuestionAnalysis(
                    question = question,
                    totalAttempts = totalSubmissions,
                    correctAttempts = correctAttempts,
                    incorrectAttempts = totalSubmissions - correctAttempts,
                    correctPercentage = if (totalSubmissions > 0) (correctAttempts.toDouble() / totalSubmissions) * 100 else 0.0,
                    responseDistribution = responseDistribution
                )
            }

            Result.success(ItemAnalysis(examId, totalSubmissions, questionAnalyses))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
