package com.arekalov.blps.kafka.dto

import java.time.Instant
import java.util.UUID

data class VacancySubmittedForModerationMessage(
    val eventId: UUID,
    val vacancyId: UUID,
    val employerId: UUID?,
    val occurredAt: Instant,
    val schemaVersion: Int = 1,
)
