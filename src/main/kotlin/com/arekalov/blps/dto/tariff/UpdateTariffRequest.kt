package com.arekalov.blps.dto.tariff

import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class UpdateTariffRequest(
    @field:Size(max = 100, message = "Name must not exceed 100 characters")
    val name: String? = null,
    @field:Positive(message = "Price must be positive")
    val price: BigDecimal? = null,
    @field:Positive(message = "Duration days must be positive")
    val durationDays: Int? = null,
    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    val description: String? = null,
)
