package com.example.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login.data.SessionManager
import com.example.login.data.model.ApiError
import com.example.login.data.model.AuthResponse
import com.example.login.data.model.LoginRequest
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
            isLoggedIn = !SessionManager.token.isNullOrBlank(),
            username = SessionManager.username.orEmpty(),
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState

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
        SessionManager.clear()
        _uiState.value = AuthUiState()
    }

    private fun authCall(block: suspend () -> AuthResponse) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = block()
                SessionManager.token = response.token
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
