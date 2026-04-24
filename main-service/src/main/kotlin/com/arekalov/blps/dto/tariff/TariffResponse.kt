package com.arekalov.blps.dto.tariff

import java.math.BigDecimal

data class TariffResponse(
    val id: String,
    val name: String,
    val price: BigDecimal,
    val durationDays: Int,
    val description: String,
)
