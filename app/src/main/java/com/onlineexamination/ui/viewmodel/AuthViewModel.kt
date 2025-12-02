package com.onlineexamination.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlineexamination.data.model.User
import com.onlineexamination.data.model.UserRole
import com.onlineexamination.data.model.StudentInfo
import com.onlineexamination.data.model.TeacherInfo
import com.onlineexamination.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: FirebaseUser? = null,
    val userData: User? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        _uiState.value = _uiState.value.copy(currentUser = repository.currentUser)
        repository.currentUser?.let { user ->
            loadUserData(user.uid)
        }
    }

    fun signUp(
        email: String,
        password: String,
        username: String,
        role: UserRole,
        studentInfo: StudentInfo? = null,
        teacherInfo: TeacherInfo? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.signUp(email, password, username, role, studentInfo, teacherInfo)
            result.getOrNull()?.let { user ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentUser = user,
                    errorMessage = null, // Clear error on success
                    successMessage = "Account created! Please verify your email before signing in."
                )
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Sign up failed"
                )
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.signIn(email.trim(), password)
            result.getOrNull()?.let { user ->
                // Reload user to get latest email verification status
                repository.reloadUser().getOrNull()
                // Update current user in state
                _uiState.value = _uiState.value.copy(
                    currentUser = repository.currentUser,
                    errorMessage = null // Clear error on success
                )
                // Load user data
                loadUserData(user.uid)
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Sign in failed"
                )
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.sendPasswordResetEmail(email.trim())
            result.getOrNull()?.let {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null, // Clear error on success
                    successMessage = "Password reset email sent! Please check your inbox."
                )
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to send password reset email"
                )
            }
        }
    }

    private fun loadUserData(uid: String) {
        viewModelScope.launch {
            val result = repository.getUserData(uid)
            result.getOrNull()?.let { user ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userData = user
                )
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to load user data"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun signOut() {
        repository.signOut()
        _uiState.value = AuthUiState()
    }

    fun reloadUser() {
        viewModelScope.launch {
            repository.reloadUser().getOrNull()?.let { user ->
                // Update current user in state with latest verification status
                _uiState.value = _uiState.value.copy(currentUser = user)
                // Reload user data if user exists
                user.uid.let { loadUserData(it) }
            }
        }
    }
}

