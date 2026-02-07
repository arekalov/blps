package com.arekalov.blps.dto.tariff

import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class UpdateTariffRequest(
    val name: String? = null,
    @field:Positive(message = "Price must be positive")
    val price: BigDecimal? = null,
    @field:Positive(message = "Duration days must be positive")
    val durationDays: Int? = null,
    val description: String? = null,
)
