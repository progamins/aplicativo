package com.example.login.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val fullName: String = "",
)

@Serializable
data class RefreshRequest(
    val refreshToken: String,
)

@Serializable
data class LogoutRequest(
    val refreshToken: String,
)

@Serializable
data class UserDto(
    val id: Long,
    val username: String,
    val fullName: String = "",
    val role: String = "estudiante",
    val createdAt: String? = null,
) {
    val isAdmin: Boolean get() = role == "admin"
}

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto,
)

@Serializable
data class MeResponse(
    val user: UserDto,
)

@Serializable
data class ApiError(
    val error: String = "",
)

// ── Justificaciones ──
@Serializable
data class Justificacion(
    val id: Long,
    val motivo: String,
    val detalle: String = "",
    val fecha: String,
    val estado: String = "pendiente",
)

@Serializable
data class JustificacionesResponse(
    val justificaciones: List<Justificacion>,
)

@Serializable
data class CreateJustificacionRequest(
    val motivo: String,
    val fecha: String,
    val detalle: String = "",
)

@Serializable
data class CreateJustificacionResponse(
    val justificacion: Justificacion,
)

// ── Admin: justificaciones de todos los estudiantes ──
@Serializable
data class AdminJustificacion(
    val id: Long,
    @SerialName("user_id") val userId: Long = 0,
    val username: String = "",
    val fullName: String = "",
    val motivo: String,
    val detalle: String = "",
    val fecha: String,
    val estado: String = "pendiente",
    val createdAt: String? = null,
)

@Serializable
data class AdminJustificacionesResponse(
    val justificaciones: List<AdminJustificacion>,
)

@Serializable
data class UpdateJustificacionEstadoRequest(
    val estado: String,
)

@Serializable
data class UpdateJustificacionEstadoResponse(
    val justificacion: Justificacion,
)

// ── Asistencias ──
@Serializable
data class Asistencia(
    val id: Long,
    val fecha: String,
    val estado: String,
    val curso: String = "",
)

@Serializable
data class AsistenciasResponse(
    val asistencias: List<Asistencia>,
)

// ── Estadísticas ──
@Serializable
data class Estadisticas(
    val justificaciones: Int = 0,
    val pendientes: Int = 0,
    val totalAsistencias: Int = 0,
    val presentes: Int = 0,
)

@Serializable
data class EstadisticasResponse(
    val estadisticas: Estadisticas,
)
