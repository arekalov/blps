package com.arekalov.blps.controller

import com.arekalov.blps.dto.auth.AuthResponse
import com.arekalov.blps.dto.auth.LoginRequest
import com.arekalov.blps.dto.auth.RefreshTokenRequest
import com.arekalov.blps.dto.auth.RegisterRequest
import com.arekalov.blps.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register new employer", description = "Register a new employer account")
    fun register(@Valid @RequestBody request: RegisterRequest): AuthResponse {
        return authService.register(request)
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login with email and password")
    fun login(@Valid @RequestBody request: LoginRequest): AuthResponse {
        return authService.login(request)
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh access token using refresh token")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): AuthResponse {
        return authService.refreshToken(request.refreshToken)
    }
}
