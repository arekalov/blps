package com.arekalov.blps.kafka.event

import java.time.Instant
import java.util.UUID

data class VacancySubmittedForModerationCommitted(
    val eventId: UUID,
    val vacancyId: UUID,
    val employerId: UUID,
    val occurredAt: Instant,
)
