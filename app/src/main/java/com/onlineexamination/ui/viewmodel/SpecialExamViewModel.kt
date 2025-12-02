package com.onlineexamination.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlineexamination.data.model.SpecialExamRequest
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

    private val _uiState = MutableStateFlow(SpecialExamUiState())
    val uiState: StateFlow<SpecialExamUiState> = _uiState.asStateFlow()

    fun createSpecialExamRequest(request: SpecialExamRequest, fileUri: Uri) {
        viewModelScope.launch {
            _uiState.value = SpecialExamUiState(isLoading = true)
            val result = repository.createSpecialExamRequest(request, fileUri)
            result.onSuccess {
                _uiState.value = SpecialExamUiState(successMessage = "Special exam request submitted successfully.")
            }.onFailure {
                _uiState.value = SpecialExamUiState(errorMessage = it.message)
            }
        }
    }
}
