package com.arekalov.blps.controller

import com.arekalov.blps.dto.auth.RegisterRequest
import com.arekalov.blps.dto.common.ErrorResponse
import com.arekalov.blps.dto.user.UserResponse
import com.arekalov.blps.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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
@Tag(name = "Authentication", description = "Registration and HTTP Basic auth")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Register new user",
        description = "Create a new user (role EMPLOYER). Auth: HTTP Basic with email and password.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "User registered successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Validation error - invalid email, password, or user already exists",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun register(@Valid @RequestBody request: RegisterRequest): UserResponse {
        return authService.register(request)
    }
}
