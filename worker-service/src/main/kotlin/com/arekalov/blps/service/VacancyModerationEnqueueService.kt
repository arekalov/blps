package com.arekalov.blps.service

import com.arekalov.blps.kafka.dto.VacancySubmittedForModerationMessage
import com.arekalov.blps.model.enum.VacancyStatus
import com.arekalov.blps.repository.VacancyRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class VacancyModerationEnqueueService(
    private val vacancyRepository: VacancyRepository,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun acceptSubmittedPayload(json: String) {
        val message = try {
            objectMapper.readValue(json, VacancySubmittedForModerationMessage::class.java)
        } catch (ex: Exception) {
            log.warn("Skip invalid vacancy submission message: {}", ex.message)
            return
        }

        val vacancy = vacancyRepository.findById(message.vacancyId).orElse(null)
        if (vacancy == null) {
            log.warn("Vacancy {} not found for submission event {}", message.vacancyId, message.eventId)
            return
        }

        if (vacancy.status != VacancyStatus.SUBMISSION_PENDING) {
            log.debug(
                "Idempotent skip: vacancy {} status {} (expected {})",
                message.vacancyId,
                vacancy.status,
                VacancyStatus.SUBMISSION_PENDING,
            )
            return
        }

        vacancy.status = VacancyStatus.PENDING_MODERATION
        vacancy.updatedAt = LocalDateTime.now()
        vacancyRepository.save(vacancy)
        log.info("Vacancy {} moved to PENDING_MODERATION (event {})", message.vacancyId, message.eventId)
    }
}
