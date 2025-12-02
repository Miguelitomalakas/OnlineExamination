package com.onlineexamination.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlineexamination.data.model.Exam
import com.onlineexamination.data.model.ExamAttempt
import com.onlineexamination.data.model.ExamResult
import com.onlineexamination.data.repository.ExamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExamUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val exams: List<Exam> = emptyList(),
    val currentExam: Exam? = null,
    val examAttempts: List<ExamAttempt> = emptyList(),
    val examResults: List<ExamResult> = emptyList(),
    val currentResult: ExamResult? = null
)

class ExamViewModel : ViewModel() {
    private val repository = ExamRepository()
    
    private val _uiState = MutableStateFlow(ExamUiState())
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    // Load active exams (for students)
    fun loadActiveExams() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.getActiveExams()
            result.getOrNull()?.let { exams ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    exams = exams,
                    errorMessage = null
                )
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to load exams"
                )
            }
        }
    }

    // Load exams by teacher
    fun loadTeacherExams(teacherId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.getExamsByTeacher(teacherId)
            result.getOrNull()?.let { exams ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    exams = exams,
                    errorMessage = null
                )
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to load exams"
                )
            }
        }
    }

    // Load exam by ID
    fun loadExamById(examId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.getExamById(examId)
            result.getOrNull()?.let { exam ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentExam = exam,
                    errorMessage = null
                )
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to load exam"
                )
            }
        }
    }

    // Create exam
    fun createExam(exam: Exam) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.createExam(exam)
            result.getOrNull()?.let { examId ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Exam created successfully!",
                    errorMessage = null
                )
                // Reload teacher exams
                exam.teacherId.let { loadTeacherExams(it) }
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to create exam"
                )
            }
        }
    }

    // Update exam
    fun updateExam(exam: Exam) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.updateExam(exam)
            result.getOrNull()?.let {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Exam updated successfully!",
                    errorMessage = null
                )
                loadTeacherExams(exam.teacherId)
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to update exam"
                )
            }
        }
    }

    // Delete exam
    fun deleteExam(examId: String, teacherId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.deleteExam(examId)
            result.getOrNull()?.let {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Exam deleted successfully!",
                    errorMessage = null
                )
                loadTeacherExams(teacherId)
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to delete exam"
                )
            }
        }
    }

    // Submit exam attempt
    fun submitExamAttempt(attempt: ExamAttempt, exam: Exam) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.submitExamAttempt(attempt, exam)
            result.getOrNull()?.let { examResult ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentResult = examResult,
                    successMessage = "Exam submitted successfully!",
                    errorMessage = null
                )
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to submit exam"
                )
            }
        }
    }

    // Load student attempts
    fun loadStudentAttempts(studentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.getAttemptsByStudent(studentId)
            result.getOrNull()?.let { attempts ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    examAttempts = attempts,
                    errorMessage = null
                )
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to load attempts"
                )
            }
        }
    }

    // Load attempts by exam (for teacher)
    fun loadExamAttempts(examId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.getAttemptsByExam(examId)
            result.getOrNull()?.let { attempts ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    examAttempts = attempts,
                    errorMessage = null
                )
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to load attempts"
                )
            }
        }
    }

    // Load result by attempt ID
    fun loadResultByAttemptId(attemptId: String, exam: Exam) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.getResultByAttemptId(attemptId, exam)
            result.getOrNull()?.let { examResult ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentResult = examResult,
                    errorMessage = null
                )
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to load result"
                )
            }
        }
    }

    // Check if student attempted exam
    fun checkIfAttempted(examId: String, studentId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.hasStudentAttemptedExam(examId, studentId)
            result.getOrNull()?.let { hasAttempted ->
                onResult(hasAttempted)
            }
        }
    }
}



