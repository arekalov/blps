package com.arekalov.blps.model.enum

enum class VacancyStatus {
    DRAFT,
    /** Ожидает доставки в очередь модерации (Kafka → worker → PENDING_MODERATION) */
    SUBMISSION_PENDING,
    PENDING_MODERATION,
    REJECTED,
    PUBLISHED,
    ARCHIVED,
    CLOSED,
}
