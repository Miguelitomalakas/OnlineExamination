package com.onlineexamination.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.onlineexamination.data.model.Exam
import com.onlineexamination.data.model.ExamAttempt
import com.onlineexamination.data.model.ExamResult
import kotlinx.coroutines.tasks.await

class ExamRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val functions = FirebaseFunctions.getInstance()

    private val examsCollection = firestore.collection("exams")
    private val attemptsCollection = firestore.collection("exam_attempts")
    private val usersCollection = firestore.collection("users")

    suspend fun createExam(exam: Exam): Result<String> {
        return try {
            val docRef = examsCollection.document()
            val examWithQuestionCount = exam.copy(id = docRef.id, questionCount = exam.questions.size)
            docRef.set(examWithQuestionCount).await()

            // After creating the exam, send notifications
            exam.gradeLevel.let { grade ->
                if (grade.isNotEmpty()) {
                    val studentTokens = getStudentTokensByGrade(grade).getOrNull()
                    if (studentTokens?.isNotEmpty() == true) {
                        sendNotification(
                            tokens = studentTokens,
                            title = "New Exam Available!",
                            message = "A new exam \"${exam.title}\" for ${exam.subject} has been posted."
                        ).await()
                    }
                }
            }

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudentTokensByGrade(gradeLevel: String): Result<List<String>> {
        return try {
            val studentQuery = usersCollection
                .whereEqualTo("role", "STUDENT")
                .get()
                .await()

            val tokens = mutableListOf<String>()
            for (userDoc in studentQuery.documents) {
                val studentProfile = userDoc.reference.collection("profiles").document("student").get().await()
                if (studentProfile.exists() && studentProfile.getString("gradeLevelToEnroll") == gradeLevel) {
                    userDoc.getString("fcmToken")?.let { tokens.add(it) }
                }
            }
            Result.success(tokens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sendNotification(tokens: List<String>, title: String, message: String) = functions
        .getHttpsCallable("sendNotification")
        .call(mapOf(
            "tokens" to tokens,
            "title" to title,
            "message" to message
        ))

    // Get exam by ID
    suspend fun getExamById(examId: String): Result<Exam> {
        return try {
            val document = examsCollection.document(examId).get().await()
            val exam = document.toObject(Exam::class.java)
                ?: return Result.failure(Exception("Exam not found"))
            Result.success(exam)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get all active exams
    suspend fun getActiveExams(): Result<List<Exam>> {
        return try {
            val now = System.currentTimeMillis()
            val snapshot = examsCollection
                .whereEqualTo("isActive", true)
                .whereGreaterThanOrEqualTo("endDate", now)
                .orderBy("endDate", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val exams = snapshot.documents.mapNotNull { it.toObject(Exam::class.java) }
            Result.success(exams)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get exams by teacher
    suspend fun getExamsByTeacher(teacherId: String): Result<List<Exam>> {
        return try {
            val snapshot = examsCollection
                .whereEqualTo("teacherId", teacherId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val exams = snapshot.documents.mapNotNull { it.toObject(Exam::class.java) }
            Result.success(exams)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update exam
    suspend fun updateExam(exam: Exam): Result<Unit> {
        return try {
            val examWithQuestionCount = exam.copy(questionCount = exam.questions.size)
            examsCollection.document(exam.id).set(examWithQuestionCount).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete exam
    suspend fun deleteExam(examId: String): Result<Unit> {
        return try {
            examsCollection.document(examId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Submit exam attempt
    suspend fun submitExamAttempt(attempt: ExamAttempt, exam: Exam): Result<ExamResult> {
        return try {
            // Calculate score
            var totalScore = 0
            val questionResults = mutableListOf<com.onlineexamination.data.model.QuestionResult>()
            
            exam.questions.forEach { question ->
                val studentAnswer = attempt.answers[question.id] ?: ""
                val isCorrect = when (question.type) {
                    com.onlineexamination.data.model.QuestionType.MULTIPLE_CHOICE,
                    com.onlineexamination.data.model.QuestionType.TRUE_FALSE -> {
                        studentAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
                    }
                    com.onlineexamination.data.model.QuestionType.SHORT_ANSWER -> {
                        // For short answer, check if answer contains key words (simplified)
                        studentAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
                    }
                }
                
                val points = if (isCorrect) question.points else 0
                totalScore += points
                
                questionResults.add(
                    com.onlineexamination.data.model.QuestionResult(
                        questionId = question.id,
                        questionText = question.questionText,
                        studentAnswer = studentAnswer,
                        correctAnswer = question.correctAnswer,
                        isCorrect = isCorrect,
                        points = points,
                        maxPoints = question.points
                    )
                )
            }
            
            val percentage = (totalScore.toDouble() / exam.totalPoints.toDouble()) * 100
            val isPassed = percentage >= exam.passingScore
            
            val submittedAttempt = attempt.copy(
                submittedAt = System.currentTimeMillis(),
                score = totalScore,
                totalPoints = exam.totalPoints,
                percentage = percentage,
                isPassed = isPassed
            )
            
            // Save attempt
            val docRef = attemptsCollection.document()
            val attemptWithId = submittedAttempt.copy(id = docRef.id)
            docRef.set(attemptWithId).await()
            
            // Create result
            val result = ExamResult(
                attemptId = docRef.id,
                examId = exam.id,
                examTitle = exam.title,
                studentId = attempt.studentId,
                studentName = attempt.studentName,
                score = totalScore,
                totalPoints = exam.totalPoints,
                percentage = percentage,
                isPassed = isPassed,
                submittedAt = System.currentTimeMillis(),
                timeSpentMinutes = attempt.timeSpentMinutes,
                questionResults = questionResults
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get attempts by student
    suspend fun getAttemptsByStudent(studentId: String): Result<List<ExamAttempt>> {
        return try {
            val snapshot = attemptsCollection
                .whereEqualTo("studentId", studentId)
                .whereNotEqualTo("submittedAt", null)
                .orderBy("submittedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val attempts = snapshot.documents.mapNotNull { it.toObject(ExamAttempt::class.java) }
            Result.success(attempts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get attempts by exam (for teacher to see all student results)
    suspend fun getAttemptsByExam(examId: String): Result<List<ExamAttempt>> {
        return try {
            val snapshot = attemptsCollection
                .whereEqualTo("examId", examId)
                .whereNotEqualTo("submittedAt", null)
                .orderBy("submittedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val attempts = snapshot.documents.mapNotNull { it.toObject(ExamAttempt::class.java) }
            Result.success(attempts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get result by attempt ID
    suspend fun getResultByAttemptId(attemptId: String, exam: Exam): Result<ExamResult> {
        return try {
            val attemptDoc = attemptsCollection.document(attemptId).get().await()
            val attempt = attemptDoc.toObject(ExamAttempt::class.java)
                ?: return Result.failure(Exception("Attempt not found"))
            
            // Reconstruct result
            val questionResults = exam.questions.map { question ->
                val studentAnswer = attempt.answers[question.id] ?: ""
                val isCorrect = when (question.type) {
                    com.onlineexamination.data.model.QuestionType.MULTIPLE_CHOICE,
                    com.onlineexamination.data.model.QuestionType.TRUE_FALSE -> {
                        studentAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
                    }
                    com.onlineexamination.data.model.QuestionType.SHORT_ANSWER -> {
                        studentAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
                    }
                }
                val points = if (isCorrect) question.points else 0
                
                com.onlineexamination.data.model.QuestionResult(
                    questionId = question.id,
                    questionText = question.questionText,
                    studentAnswer = studentAnswer,
                    correctAnswer = question.correctAnswer,
                    isCorrect = isCorrect,
                    points = points,
                    maxPoints = question.points
                )
            }
            
            val result = ExamResult(
                attemptId = attempt.id,
                examId = exam.id,
                examTitle = exam.title,
                studentId = attempt.studentId,
                studentName = attempt.studentName,
                score = attempt.score,
                totalPoints = attempt.totalPoints,
                percentage = attempt.percentage,
                isPassed = attempt.isPassed,
                submittedAt = attempt.submittedAt ?: System.currentTimeMillis(),
                timeSpentMinutes = attempt.timeSpentMinutes,
                questionResults = questionResults
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Check if student already attempted exam
    suspend fun hasStudentAttemptedExam(examId: String, studentId: String): Result<Boolean> {
        return try {
            val snapshot = attemptsCollection
                .whereEqualTo("examId", examId)
                .whereEqualTo("studentId", studentId)
                .whereNotEqualTo("submittedAt", null)
                .limit(1)
                .get()
                .await()
            
            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}