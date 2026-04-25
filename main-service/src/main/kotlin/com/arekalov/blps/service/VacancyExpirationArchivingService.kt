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

    @Transactional
    fun archiveExpiredPublishedVacancies(): Int {
        val now = LocalDateTime.now(clock)
        return vacancyRepository.archivePublishedExpiredBefore(asOf = now, updatedAt = now)
    }
}
