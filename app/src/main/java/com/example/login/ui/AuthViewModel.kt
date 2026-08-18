package com.example.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login.data.SessionManager
import com.example.login.data.model.ApiError
import com.example.login.data.model.Asistencia
import com.example.login.data.model.AuthResponse
import com.example.login.data.model.CreateJustificacionRequest
import com.example.login.data.model.Curso
import com.example.login.data.model.Horario
import com.example.login.data.model.Justificacion
import com.example.login.data.model.LoginRequest
import com.example.login.data.model.LogoutRequest
import com.example.login.data.model.Pago
import com.example.login.data.model.RegisterRequest
import com.example.login.data.model.UpdateProfileRequest
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
    val dni: String = "",
    val programa: String = "",
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

/** Perfil de identificación editable (correo, dirección, teléfono). */
data class ProfileUiState(
    val email: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
)

data class PagosUiState(
    val isLoading: Boolean = false,
    val items: List<Pago> = emptyList(),
    val ubicaciones: List<String> = emptyList(),
    val error: String? = null,
)

data class HorariosUiState(
    val isLoading: Boolean = false,
    val items: List<Horario> = emptyList(),
    val error: String? = null,
)

data class CursosUiState(
    val isLoading: Boolean = false,
    val items: List<Curso> = emptyList(),
    val error: String? = null,
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            // Si hay sesión guardada, se valida contra la API al iniciar.
            isLoading = !SessionManager.accessToken.isNullOrBlank(),
            isLoggedIn = false,
            username = SessionManager.username.orEmpty(),
            fullName = SessionManager.fullName.orEmpty(),
            dni = SessionManager.dni.orEmpty(),
            programa = SessionManager.programa.orEmpty(),
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _justificaciones = MutableStateFlow(JustificacionesUiState())
    val justificaciones: StateFlow<JustificacionesUiState> = _justificaciones

    private val _asistencias = MutableStateFlow(AsistenciasUiState())
    val asistencias: StateFlow<AsistenciasUiState> = _asistencias

    private val _stats = MutableStateFlow(StatsUiState())
    val stats: StateFlow<StatsUiState> = _stats

    private val _profile = MutableStateFlow(ProfileUiState())
    val profile: StateFlow<ProfileUiState> = _profile

    private val _pagos = MutableStateFlow(PagosUiState())
    val pagos: StateFlow<PagosUiState> = _pagos

    private val _horarios = MutableStateFlow(HorariosUiState())
    val horarios: StateFlow<HorariosUiState> = _horarios

    private val _cursos = MutableStateFlow(CursosUiState())
    val cursos: StateFlow<CursosUiState> = _cursos

    init {
        if (!SessionManager.accessToken.isNullOrBlank()) validateSession()
    }

    /** Valida la sesión guardada contra /api/auth/me (renueva token si hace falta). */
    private fun validateSession() {
        viewModelScope.launch {
            try {
                val me = ApiClient.api.me()
                storeUser(me.user.username, me.user.fullName, me.user.dni, me.user.programa)
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

    // ── Perfil de identificación ──

    /** Carga el perfil editable (correo, dirección, teléfono) desde /me. */
    fun loadProfile() {
        viewModelScope.launch {
            _profile.value = _profile.value.copy(isLoading = true, error = null)
            try {
                val user = ApiClient.api.me().user
                applyProfile(user.email, user.direccion, user.telefono)
                _profile.value = _profile.value.copy(isLoading = false)
            } catch (e: Exception) {
                _profile.value = _profile.value.copy(isLoading = false, error = e.toUserMessage())
            }
        }
    }

    fun updateProfile(email: String, direccion: String, telefono: String) {
        viewModelScope.launch {
            _profile.value = _profile.value.copy(isSaving = true, error = null, saved = false)
            try {
                val user = ApiClient.api
                    .updateProfile(UpdateProfileRequest(email.trim(), direccion.trim(), telefono.trim()))
                    .user
                applyProfile(user.email, user.direccion, user.telefono)
                _profile.value = _profile.value.copy(isSaving = false, saved = true)
            } catch (e: Exception) {
                _profile.value = _profile.value.copy(isSaving = false, error = e.toUserMessage())
            }
        }
    }

    private fun applyProfile(email: String, direccion: String, telefono: String) {
        SessionManager.email = email
        SessionManager.direccion = direccion
        SessionManager.telefono = telefono
        _profile.value = _profile.value.copy(email = email, direccion = direccion, telefono = telefono)
    }

    // ── Campus: Pagos / Horarios / Cursos ──

    fun loadPagos() {
        viewModelScope.launch {
            _pagos.value = _pagos.value.copy(isLoading = true, error = null)
            try {
                val res = ApiClient.api.pagos()
                _pagos.value = PagosUiState(items = res.pagos, ubicaciones = res.ubicaciones)
            } catch (e: Exception) {
                _pagos.value = PagosUiState(error = e.toUserMessage())
            }
        }
    }

    fun loadHorarios() {
        viewModelScope.launch {
            _horarios.value = _horarios.value.copy(isLoading = true, error = null)
            try {
                val res = ApiClient.api.horarios()
                _horarios.value = HorariosUiState(items = res.horarios)
            } catch (e: Exception) {
                _horarios.value = HorariosUiState(error = e.toUserMessage())
            }
        }
    }

    fun loadCursos() {
        viewModelScope.launch {
            _cursos.value = _cursos.value.copy(isLoading = true, error = null)
            try {
                val res = ApiClient.api.cursos()
                _cursos.value = CursosUiState(items = res.cursos)
            } catch (e: Exception) {
                _cursos.value = CursosUiState(error = e.toUserMessage())
            }
        }
    }

    // ── Helpers ──

    private fun storeUser(username: String, fullName: String, dni: String = "", programa: String = "") {
        SessionManager.username = username
        SessionManager.fullName = fullName
        SessionManager.dni = dni
        SessionManager.programa = programa
        _uiState.value = AuthUiState(
            isLoggedIn = true,
            username = username,
            fullName = fullName,
            dni = dni,
            programa = programa,
        )
        _profile.value = ProfileUiState(
            email = SessionManager.email.orEmpty(),
            direccion = SessionManager.direccion.orEmpty(),
            telefono = SessionManager.telefono.orEmpty(),
        )
    }

    private fun authCall(block: suspend () -> AuthResponse) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = block()
                SessionManager.accessToken = response.accessToken
                SessionManager.refreshToken = response.refreshToken
                storeUser(
                    response.user.username,
                    response.user.fullName,
                    response.user.dni,
                    response.user.programa,
                )
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
