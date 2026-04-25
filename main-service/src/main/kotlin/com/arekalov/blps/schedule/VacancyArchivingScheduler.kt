package com.arekalov.blps.schedule

import com.arekalov.blps.service.VacancyExpirationArchivingService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["blps.scheduling.enabled"], havingValue = "true", matchIfMissing = true)
class VacancyArchivingScheduler(
    private val vacancyExpirationArchivingService: VacancyExpirationArchivingService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "\${blps.scheduling.published-vacancy-archive-cron}")
    fun archiveExpiredPublishedVacancies() {
        val updated = vacancyExpirationArchivingService.archiveExpiredPublishedVacancies()
        if (updated > 0) {
            log.info("Auto-archived {} published vacancy/vacancies (tariff period expired)", updated)
        } else {
            log.debug("Auto-archive run: no published vacancies past tariff end")
        }
    }
}
