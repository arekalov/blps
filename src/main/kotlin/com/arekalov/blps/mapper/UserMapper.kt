package com.arekalov.blps.mapper

import com.arekalov.blps.dto.auth.AuthResponse
import com.arekalov.blps.dto.auth.RegisterRequest
import com.arekalov.blps.dto.user.UserResponse
import com.arekalov.blps.model.User
import com.arekalov.blps.model.enum.UserRole

fun User.toResponse() = UserResponse(
    id = id!!.toString(),
    email = email,
    companyName = companyName,
    role = role.name,
)

fun User.toAuthResponse(accessToken: String, refreshToken: String) = AuthResponse(
    accessToken = accessToken,
    refreshToken = refreshToken,
    userId = id!!.toString(),
    email = email,
    role = role.name,
)

fun RegisterRequest.toEntity(encodedPassword: String) = User(
    id = null,
    email = email,
    passwordHash = encodedPassword,
    companyName = companyName,
    role = UserRole.EMPLOYER,
)
