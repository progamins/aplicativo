package com.example.login.data.remote

import com.example.login.data.model.AuthResponse
import com.example.login.data.model.LoginRequest
import com.example.login.data.model.LogoutRequest
import com.example.login.data.model.MeResponse
import com.example.login.data.model.RefreshRequest
import com.example.login.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("api/auth/me")
    suspend fun me(): MeResponse

    @POST("api/auth/logout")
    suspend fun logout(@Body body: LogoutRequest)
}

/** Variante síncrona (Call) solo para renovar el token desde el interceptor. */
interface RefreshService {

    @POST("api/auth/refresh")
    fun refresh(@Body body: RefreshRequest): retrofit2.Call<AuthResponse>
}
