package com.example.login.data.remote

import com.example.login.data.model.AsistenciasResponse
import com.example.login.data.model.AuthResponse
import com.example.login.data.model.CreateJustificacionRequest
import com.example.login.data.model.CreateJustificacionResponse
import com.example.login.data.model.CursosResponse
import com.example.login.data.model.EstadisticasResponse
import com.example.login.data.model.HorariosResponse
import com.example.login.data.model.JustificacionesResponse
import com.example.login.data.model.LoginRequest
import com.example.login.data.model.LogoutRequest
import com.example.login.data.model.MeResponse
import com.example.login.data.model.PagosResponse
import com.example.login.data.model.RefreshRequest
import com.example.login.data.model.RegisterRequest
import com.example.login.data.model.UpdateProfileRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface ApiService {

    // ── Auth ──
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("api/auth/me")
    suspend fun me(): MeResponse

    @PATCH("api/auth/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): MeResponse

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

    // ── Campus ──
    @GET("api/pagos")
    suspend fun pagos(): PagosResponse

    @GET("api/horarios")
    suspend fun horarios(): HorariosResponse

    @GET("api/cursos")
    suspend fun cursos(): CursosResponse
}

/** Variante síncrona (Call) solo para renovar el token desde el interceptor. */
interface RefreshService {

    @POST("api/auth/refresh")
    fun refresh(@Body body: RefreshRequest): retrofit2.Call<AuthResponse>
}
