package com.onlineexamination.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlineexamination.data.model.StudentLog
import com.onlineexamination.data.repository.LogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LogUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val studentLogs: List<StudentLog> = emptyList()
)

class LogViewModel : ViewModel() {

    private val repository = LogRepository()

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    fun addLog(log: StudentLog) {
        viewModelScope.launch {
            repository.addLog(log)
        }
    }

    fun getStudentLogs(studentId: String) {
        viewModelScope.launch {
            _uiState.value = LogUiState(isLoading = true)
            val result = repository.getLogsByStudent(studentId)
            result.onSuccess {
                _uiState.value = LogUiState(studentLogs = it)
            }.onFailure {
                _uiState.value = LogUiState(errorMessage = it.message)
            }
        }
    }
}
