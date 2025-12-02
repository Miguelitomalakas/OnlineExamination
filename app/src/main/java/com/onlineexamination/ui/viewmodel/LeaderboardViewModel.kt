package com.onlineexamination.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlineexamination.data.model.Leaderboard
import com.onlineexamination.data.repository.LeaderboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val leaderboard: Leaderboard? = null
)

class LeaderboardViewModel : ViewModel() {
    private val repository = LeaderboardRepository()

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    fun getLeaderboard(term: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.getLeaderboard(term)
            result.getOrNull()?.let {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    leaderboard = it,
                    errorMessage = null
                )
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to load leaderboard"
                )
            }
        }
    }
}
