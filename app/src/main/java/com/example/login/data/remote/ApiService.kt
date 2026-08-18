package com.example.login.data.remote

import com.example.login.data.model.AdminJustificacionesResponse
import com.example.login.data.model.AsistenciasResponse
import com.example.login.data.model.AuthResponse
import com.example.login.data.model.CreateJustificacionRequest
import com.example.login.data.model.CreateJustificacionResponse
import com.example.login.data.model.EstadisticasResponse
import com.example.login.data.model.JustificacionesResponse
import com.example.login.data.model.LoginRequest
import com.example.login.data.model.LogoutRequest
import com.example.login.data.model.MeResponse
import com.example.login.data.model.RefreshRequest
import com.example.login.data.model.RegisterRequest
import com.example.login.data.model.UpdateJustificacionEstadoRequest
import com.example.login.data.model.UpdateJustificacionEstadoResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // ── Auth ──
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("api/auth/me")
    suspend fun me(): MeResponse

    @POST("api/auth/logout")
    suspend fun logout(@Body body: LogoutRequest)

    // ── Académico ──
    @GET("api/justificaciones")
    suspend fun justificaciones(): JustificacionesResponse

    @POST("api/justificaciones")
    suspend fun createJustificacion(@Body body: CreateJustificacionRequest): CreateJustificacionResponse

    @GET("api/asistencias")
    suspend fun asistencias(): AsistenciasResponse

    @GET("api/estadisticas")
    suspend fun estadisticas(): EstadisticasResponse

    // ── Admin ──
    @GET("api/admin/justificaciones")
    suspend fun adminJustificaciones(): AdminJustificacionesResponse

    @PATCH("api/admin/justificaciones/{id}")
    suspend fun updateJustificacionEstado(
        @Path("id") id: Long,
        @Body body: UpdateJustificacionEstadoRequest,
    ): UpdateJustificacionEstadoResponse
}

/** Variante síncrona (Call) solo para renovar el token desde el interceptor. */
interface RefreshService {

    @POST("api/auth/refresh")
    fun refresh(@Body body: RefreshRequest): retrofit2.Call<AuthResponse>
}
