package com.netflixclone.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netflixclone.data.repository.AuthRepository
import com.netflixclone.network.services.TokenResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.login(email, password)
            if (result.isSuccess) {
                _authState.value = AuthState.Success(result.getOrNull()!!)
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
                // Reset to Idle after error or handle in UI
            }
        }
    }

    fun signup(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signup(email, password)
            if (result.isSuccess) {
                _authState.value = AuthState.Success(result.getOrNull()!!)
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authRepository.googleLogin(idToken)

            if (result.isSuccess) {
                _authState.value = AuthState.Success(result.getOrNull()!!)
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Google login failed"
                )
            }
        }
    }

    /**
     * Initiates GitHub OAuth flow
     * This will trigger the UI to open Chrome Custom Tab with GitHub OAuth URL
     */
    fun launchGitHubLogin(baseUrl: String) {
        val githubAuthUrl = "${baseUrl}auth/github"
        _authState.value = AuthState.GitHubAuthRequired(githubAuthUrl)
    }

    /**
     * Called when app receives the OAuth callback
     * Verifies the session by making an authenticated request
     */
    fun verifyGitHubSession() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            // Make a test request to a protected endpoint to verify session
            val result = authRepository.verifySession()
            
            if (result.isSuccess) {
                // Session is valid, create a dummy token response for compatibility
                _authState.value = AuthState.Success(
                    TokenResponse(
                        token = "session_active",
                        subscribed = true,
                        message = "GitHub login successful"
                    )
                )
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "GitHub login failed"
                )
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val response: TokenResponse) : AuthState()
    data class Error(val message: String) : AuthState()
    data class GitHubAuthRequired(val url: String) : AuthState()
}
