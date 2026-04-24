package com.arekalov.blps.dto.tariff

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class CreateTariffRequest(
    @field:NotBlank(message = "Name cannot be blank")
    @field:Size(max = 100, message = "Name must not exceed 100 characters")
    val name: String,
    @field:NotNull(message = "Price cannot be null")
    @field:Positive(message = "Price must be positive")
    val price: BigDecimal,
    @field:NotNull(message = "Duration days cannot be null")
    @field:Positive(message = "Duration days must be positive")
    val durationDays: Int,
    @field:NotBlank(message = "Description cannot be blank")
    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    val description: String,
)
