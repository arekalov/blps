package com.arekalov.blps.eis

import com.arekalov.blps.model.enum.VacancyStatus
import com.arekalov.blps.repository.VacancyRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class BitrixInboundWebhookService(
    private val vacancyRepository: VacancyRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun closePublishedVacancyById(vacancyId: UUID): CloseResult {
        val vacancy = vacancyRepository.findById(vacancyId).orElse(null)
            ?: return CloseResult.NOT_FOUND
        if (vacancy.status == VacancyStatus.CLOSED) {
            return CloseResult.ALREADY_CLOSED
        }
        if (vacancy.status != VacancyStatus.PUBLISHED) {
            return CloseResult.IGNORED_STATUS
        }
        vacancy.status = VacancyStatus.CLOSED
        vacancy.updatedAt = LocalDateTime.now()
        vacancyRepository.save(vacancy)
        log.info("Bitrix inbound closed vacancy: vacancyId={}", vacancyId)
        return CloseResult.CLOSED
    }
}

enum class CloseResult {
    CLOSED,
    ALREADY_CLOSED,
    IGNORED_STATUS,
    NOT_FOUND,
}
