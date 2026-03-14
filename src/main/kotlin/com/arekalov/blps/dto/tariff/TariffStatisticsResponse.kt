package com.arekalov.blps.dto.tariff

import java.math.BigDecimal
import java.time.LocalDateTime

data class TariffStatisticsResponse(
    val tariffId: String,
    val tariffName: String,
    val currentPrice: BigDecimal,
    val currentDurationDays: Int,
    val usageCount: Long,
    val totalRevenue: BigDecimal,
    val firstUsedAt: LocalDateTime?,
    val lastUsedAt: LocalDateTime?,
)
