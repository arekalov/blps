package com.arekalov.blps.dto.moderation

import jakarta.validation.constraints.NotBlank

data class RejectVacancyRequest(
    @field:NotBlank(message = "Rejection reason is required")
    val reason: String,
)
