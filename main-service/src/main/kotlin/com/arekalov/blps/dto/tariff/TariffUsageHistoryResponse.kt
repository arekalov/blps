package com.arekalov.blps.dto.tariff

import java.math.BigDecimal
import java.time.LocalDateTime

data class TariffUsageHistoryResponse(
    val id: String,
    val vacancyId: String,
    val vacancyTitle: String,
    val tariffId: String,
    val tariffName: String,
    val employerId: String,
    val employerCompanyName: String,
    val moderatorId: String,
    val moderatorEmail: String,
    val price: BigDecimal,
    val durationDays: Int,
    val publishedAt: LocalDateTime,
)
