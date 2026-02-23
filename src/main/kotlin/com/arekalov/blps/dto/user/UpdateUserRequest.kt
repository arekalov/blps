package com.arekalov.blps.dto.user

import com.arekalov.blps.model.enum.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class UpdateUserRequest(
    @field:Email(message = "Invalid email format")
    @field:Size(max = 255, message = "Email must not exceed 255 characters")
    val email: String? = null,

    @field:Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    val password: String? = null,

    @field:Size(max = 255, message = "Company name must not exceed 255 characters")
    val companyName: String? = null,

    val role: UserRole? = null,
)
