package com.arekalov.blps.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Invalid email format")
    @field:Size(max = 255, message = "Email must not exceed 255 characters")
    val email: String,
    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    val password: String,
    @field:NotBlank(message = "Company name cannot be blank")
    @field:Size(max = 255, message = "Company name must not exceed 255 characters")
    val companyName: String,
    val isAdmin: Boolean = false,
)
