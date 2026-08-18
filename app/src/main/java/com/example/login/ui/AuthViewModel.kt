package com.example.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login.data.SessionManager
import com.example.login.data.model.ApiError
import com.example.login.data.model.AuthResponse
import com.example.login.data.model.LoginRequest
import com.example.login.data.model.LogoutRequest
import com.example.login.data.model.RegisterRequest
import com.example.login.data.remote.ApiClient
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val username: String = "",
    val error: String? = null,
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            // Si hay sesión guardada, se valida contra la API al iniciar.
            isLoading = !SessionManager.accessToken.isNullOrBlank(),
            isLoggedIn = false,
            username = SessionManager.username.orEmpty(),
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        if (!SessionManager.accessToken.isNullOrBlank()) validateSession()
    }

    /** Valida la sesión guardada contra /api/auth/me (renueva token si hace falta). */
    private fun validateSession() {
        viewModelScope.launch {
            try {
                val me = ApiClient.api.me()
                SessionManager.username = me.user.username
                _uiState.value = AuthUiState(isLoggedIn = true, username = me.user.username)
            } catch (_: Exception) {
                SessionManager.clear()
                _uiState.value = AuthUiState()
            }
        }
    }

    fun login(username: String, password: String) {
        authCall {
            ApiClient.api.login(LoginRequest(username = username.trim(), password = password))
        }
    }

    fun register(username: String, password: String, fullName: String) {
        authCall {
            ApiClient.api.register(
                RegisterRequest(
                    username = username.trim(),
                    password = password,
                    fullName = fullName.trim(),
                )
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            val refreshToken = SessionManager.refreshToken
            SessionManager.clear()
            _uiState.value = AuthUiState()
            if (refreshToken != null) {
                // Revocación del refresh token en el servidor (best-effort).
                try {
                    ApiClient.api.logout(LogoutRequest(refreshToken))
                } catch (_: Exception) {
                    // Sin conexión: la sesión local ya se limpió.
                }
            }
        }
    }

    private fun authCall(block: suspend () -> AuthResponse) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = block()
                SessionManager.accessToken = response.accessToken
                SessionManager.refreshToken = response.refreshToken
                SessionManager.username = response.user.username
                _uiState.value = AuthUiState(isLoggedIn = true, username = response.user.username)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.toUserMessage())
            }
        }
    }

    private fun Exception.toUserMessage(): String = when (this) {
        is HttpException -> {
            val body = response()?.errorBody()?.string()
            if (body.isNullOrBlank()) {
                "Error del servidor (código ${code()})"
            } else {
                try {
                    ApiClient.json.decodeFromString(ApiError.serializer(), body).error
                        .ifBlank { "Error del servidor (código ${code()})" }
                } catch (_: Exception) {
                    "Error del servidor (código ${code()})"
                }
            }
        }
        is IOException -> "No se pudo conectar al servidor. ¿Está corriendo la API?"
        else -> message ?: "Error inesperado"
    }
}
