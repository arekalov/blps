package com.arekalov.blps.dto.auth

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val email: String,
    val role: String,
)
