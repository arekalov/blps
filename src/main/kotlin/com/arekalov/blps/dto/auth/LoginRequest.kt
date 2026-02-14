package com.arekalov.blps.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Invalid email format")
    @field:Size(max = 255, message = "Email must not exceed 255 characters")
    val email: String,
    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(max = 100, message = "Password must not exceed 100 characters")
    val password: String,
)
