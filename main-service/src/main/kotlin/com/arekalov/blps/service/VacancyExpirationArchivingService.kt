package com.arekalov.blps.service

import com.arekalov.blps.repository.VacancyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@Service
class VacancyExpirationArchivingService(
    private val vacancyRepository: VacancyRepository,
    private val clock: Clock,
) {

    /**
     * Помечает как ARCHIVED все PUBLISHED, у которых истёк срок по тарифу:
     * `published_at + duration_days` (см. [VacancyRepository.archivePublishedExpiredBefore]).
     * [Clock] — зона JVM по умолчанию (как у `publishedAt` при одобрении), не UTC в изоляции.
     */
    @Transactional
    fun archiveExpiredPublishedVacancies(): Int {
        val now = LocalDateTime.now(clock)
        return vacancyRepository.archivePublishedExpiredBefore(asOf = now, updatedAt = now)
    }
}
