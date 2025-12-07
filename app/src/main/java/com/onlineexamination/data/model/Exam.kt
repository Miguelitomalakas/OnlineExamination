package com.onlineexamination.data.model

data class Exam(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val subject: String = "",
    val gradeLevel: String = "",
    val durationMinutes: Int = 60,
    val totalPoints: Int = 100,
    val passingScore: Int = 60,
    val teacherId: String = "",
    val teacherName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L), // 7 days default
    val startTime: String = "",
    val endTime: String = "",
    val isActive: Boolean = true,
    val questions: List<Question> = emptyList(),
    val questionCount: Int = 0,
    val term: String = ""
)

data class Question(
    val id: String = "",
    val questionText: String = "",
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val points: Int = 10,
    val explanation: String = ""
)

enum class QuestionType {
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    SHORT_ANSWER
}

data class ExamAttempt(
    val id: String = "",
    val examId: String = "",
    val examTitle: String = "",
    val teacherId: String = "", // Added for easier querying
    val studentId: String = "",
    val studentName: String = "",
    val studentEmail: String = "",
    val startedAt: Long = System.currentTimeMillis(),
    val submittedAt: Long? = null,
    val answers: Map<String, String> = emptyMap(), // questionId -> answer
    val score: Int = 0,
    val totalPoints: Int = 100,
    val percentage: Double = 0.0,
    val isPassed: Boolean = false,
    val timeSpentMinutes: Int = 0
)

data class ExamResult(
    val attemptId: String = "",
    val examId: String = "",
    val examTitle: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val score: Int = 0,
    val totalPoints: Int = 100,
    val percentage: Double = 0.0,
    val isPassed: Boolean = false,
    val submittedAt: Long = System.currentTimeMillis(),
    val timeSpentMinutes: Int = 0,
    val questionResults: List<QuestionResult> = emptyList()
)

data class QuestionResult(
    val questionId: String = "",
    val questionText: String = "",
    val studentAnswer: String = "",
    val correctAnswer: String = "",
    val isCorrect: Boolean = false,
    val points: Int = 0,
    val maxPoints: Int = 10
)
