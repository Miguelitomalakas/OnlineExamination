package com.onlineexamination.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlineexamination.data.model.ItemAnalysis
import com.onlineexamination.data.repository.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val itemAnalysis: ItemAnalysis? = null
)

class AnalyticsViewModel : ViewModel() {

    private val analyticsRepository = AnalyticsRepository()

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    fun getItemAnalysis(examId: String) {
        viewModelScope.launch {
            _uiState.value = AnalyticsUiState(isLoading = true)
            val result = analyticsRepository.getItemAnalysis(examId)
            result.onSuccess {
                _uiState.value = AnalyticsUiState(itemAnalysis = it)
            }.onFailure {
                _uiState.value = AnalyticsUiState(errorMessage = it.message)
            }
        }
    }
}
