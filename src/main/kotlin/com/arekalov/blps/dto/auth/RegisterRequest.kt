package com.arekalov.blps.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Invalid email format")
    val email: String,
    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String,
    @field:NotBlank(message = "Company name cannot be blank")
    val companyName: String,
)
