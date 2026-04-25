package com.arekalov.blps.eis.event

import java.util.UUID

data class VacancyPublishedForBitrixCommitted(
    val vacancyId: UUID,
    val title: String,
    val employerCompanyName: String,
    val tariffName: String,
    val tariffDurationDays: Int,
)
