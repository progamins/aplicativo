package com.example.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login.data.SessionManager
import com.example.login.data.model.ApiError
import com.example.login.data.model.Asistencia
import com.example.login.data.model.AuthResponse
import com.example.login.data.model.CreateJustificacionRequest
import com.example.login.data.model.Justificacion
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
    val fullName: String = "",
    val error: String? = null,
)

data class JustificacionesUiState(
    val isLoading: Boolean = false,
    val items: List<Justificacion> = emptyList(),
    val error: String? = null,
)

data class AsistenciasUiState(
    val isLoading: Boolean = false,
    val items: List<Asistencia> = emptyList(),
    val error: String? = null,
)

data class StatsUiState(
    val justificaciones: Int = 0,
    val pendientes: Int = 0,
    val totalAsistencias: Int = 0,
    val presentes: Int = 0,
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            // Si hay sesión guardada, se valida contra la API al iniciar.
            isLoading = !SessionManager.accessToken.isNullOrBlank(),
            isLoggedIn = false,
            username = SessionManager.username.orEmpty(),
            fullName = SessionManager.fullName.orEmpty(),
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _justificaciones = MutableStateFlow(JustificacionesUiState())
    val justificaciones: StateFlow<JustificacionesUiState> = _justificaciones

    private val _asistencias = MutableStateFlow(AsistenciasUiState())
    val asistencias: StateFlow<AsistenciasUiState> = _asistencias

    private val _stats = MutableStateFlow(StatsUiState())
    val stats: StateFlow<StatsUiState> = _stats

    init {
        if (!SessionManager.accessToken.isNullOrBlank()) validateSession()
    }

    /** Valida la sesión guardada contra /api/auth/me (renueva token si hace falta). */
    private fun validateSession() {
        viewModelScope.launch {
            try {
                val me = ApiClient.api.me()
                storeUser(me.user.username, me.user.fullName)
                loadDashboard()
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

    // ── Datos académicos ──

    fun loadDashboard() {
        loadJustificaciones()
        loadAsistencias()
        loadStats()
    }

    fun loadJustificaciones() {
        viewModelScope.launch {
            _justificaciones.value = _justificaciones.value.copy(isLoading = true, error = null)
            try {
                val res = ApiClient.api.justificaciones()
                _justificaciones.value = JustificacionesUiState(items = res.justificaciones)
            } catch (e: Exception) {
                _justificaciones.value = JustificacionesUiState(error = e.toUserMessage())
            }
        }
    }

    fun loadAsistencias() {
        viewModelScope.launch {
            _asistencias.value = _asistencias.value.copy(isLoading = true, error = null)
            try {
                val res = ApiClient.api.asistencias()
                _asistencias.value = AsistenciasUiState(items = res.asistencias)
            } catch (e: Exception) {
                _asistencias.value = AsistenciasUiState(error = e.toUserMessage())
            }
        }
    }

    fun createJustificacion(motivo: String, fecha: String, detalle: String, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                ApiClient.api.createJustificacion(
                    CreateJustificacionRequest(motivo = motivo.trim(), fecha = fecha.trim(), detalle = detalle.trim())
                )
                loadJustificaciones()
                loadStats()
                onDone()
            } catch (e: Exception) {
                _justificaciones.value = _justificaciones.value.copy(error = e.toUserMessage())
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                val res = ApiClient.api.estadisticas()
                val s = res.estadisticas
                _stats.value = StatsUiState(
                    justificaciones = s.justificaciones,
                    pendientes = s.pendientes,
                    totalAsistencias = s.totalAsistencias,
                    presentes = s.presentes,
                )
            } catch (_: Exception) {
                // El dashboard se mantiene con los valores anteriores.
            }
        }
    }

    // ── Helpers ──

    private fun storeUser(username: String, fullName: String) {
        SessionManager.username = username
        SessionManager.fullName = fullName
        _uiState.value = AuthUiState(isLoggedIn = true, username = username, fullName = fullName)
    }

    private fun authCall(block: suspend () -> AuthResponse) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = block()
                SessionManager.accessToken = response.accessToken
                SessionManager.refreshToken = response.refreshToken
                storeUser(response.user.username, response.user.fullName)
                loadDashboard()
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
