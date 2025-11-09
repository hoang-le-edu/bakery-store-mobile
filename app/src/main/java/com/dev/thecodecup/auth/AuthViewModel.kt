package com.dev.thecodecup.auth

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dev.thecodecup.model.network.NetworkModule
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val error: String? = null,
    val isSignedIn: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authManager = GoogleAuthManager.getInstance(application)
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        checkCurrentUser()
    }
    
    /**
     * Check if user is already signed in
     */
    private fun checkCurrentUser() {
        val currentUser = authManager.getCurrentUser()
        _authState.value = _authState.value.copy(
            user = currentUser,
            isSignedIn = currentUser != null
        )
        
        // Setup token provider if user is already signed in
        if (currentUser != null) {
            setupTokenProvider()
        }
    }
    
    /**
     * Get Google Sign-In Intent
     * Launch this intent to start the sign-in flow
     */
    fun getSignInIntent(): Intent {
        _authState.value = _authState.value.copy(error = null)
        return authManager.getSignInIntent()
    }
    
    /**
     * Handle sign-in result from the sign-in activity
     */
    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val result = authManager.handleSignInResult(data)
            
            result.onSuccess { user ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    user = user,
                    isSignedIn = true,
                    error = null
                )
                
                // Update NetworkModule token provider
                setupTokenProvider()
                
                Log.d("AuthViewModel", "Sign in successful: ${user.email}")
            }.onFailure { exception ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Sign in failed"
                )
                Log.e("AuthViewModel", "Sign in failed", exception)
            }
        }
    }
    
    /**
     * Setup token provider for API calls
     */
    private fun setupTokenProvider() {
        NetworkModule.tokenProvider = {
            try {
                // Get current user's ID token synchronously
                // Note: This should be called from a background thread
                val user = authManager.getCurrentUser()
                user?.getIdToken(false)?.result?.token
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to get token", e)
                null
            }
        }
    }
    
    /**
     * Get Firebase ID token for API calls
     */
    fun getIdToken(onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = authManager.getIdToken()
            onResult(result)
        }
    }
    
    /**
     * Sign out
     */
    fun signOut() {
        viewModelScope.launch {
            authManager.signOut()
            _authState.value = AuthState(isSignedIn = false)
        }
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}

