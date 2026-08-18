package com.example.login.data.remote

import com.example.login.data.model.AuthResponse
import com.example.login.data.model.LoginRequest
import com.example.login.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse
}
