package com.arekalov.blps.kafka.dto

import java.time.Instant
import java.util.UUID

/**
 * JSON payload sent to Kafka after a vacancy is committed as [com.arekalov.blps.model.enum.VacancyStatus.SUBMISSION_PENDING].
 */
data class VacancySubmittedForModerationMessage(
    val eventId: UUID,
    val vacancyId: UUID,
    val employerId: UUID?,
    val occurredAt: Instant,
    val schemaVersion: Int = 1,
)
