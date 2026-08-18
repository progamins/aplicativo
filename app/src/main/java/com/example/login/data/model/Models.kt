package com.example.login.data.model

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
data class UserDto(
    val id: Long,
    val username: String,
    val fullName: String = "",
    val createdAt: String = "",
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto,
)

@Serializable
data class ApiError(
    val error: String = "",
)
