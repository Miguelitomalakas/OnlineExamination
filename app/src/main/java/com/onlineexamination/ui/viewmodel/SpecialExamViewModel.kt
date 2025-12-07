package com.onlineexamination.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlineexamination.data.model.SpecialExamRequest
import com.onlineexamination.data.repository.FileRepository
import com.onlineexamination.data.repository.SpecialExamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SpecialExamUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class SpecialExamViewModel : ViewModel() {
    private val repository = SpecialExamRepository()
    private val fileRepository = FileRepository()

    private val _uiState = MutableStateFlow(SpecialExamUiState())
    val uiState: StateFlow<SpecialExamUiState> = _uiState.asStateFlow()

    fun uploadFile(uri: Uri, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val result = fileRepository.uploadFile(uri, "special_exam_requests")
            onResult(result.getOrNull())
        }
    }

    fun submitRequest(
        studentId: String,
        studentName: String,
        examId: String,
        examTitle: String,
        reason: String,
        description: String,
        fileUrl: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val request = SpecialExamRequest(
                studentId = studentId,
                studentName = studentName,
                examId = examId,
                examTitle = examTitle,
                // Will be fetched in the repository
                teacherId = "", 
                reason = reason,
                description = description,
                proofFileUrl = fileUrl
            )
            val result = repository.submitRequest(request)
            onResult(result.isSuccess)
        }
    }
}
