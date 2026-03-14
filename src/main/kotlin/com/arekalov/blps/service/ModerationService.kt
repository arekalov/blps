package com.arekalov.blps.service

import com.arekalov.blps.dto.common.PagedResponse
import com.arekalov.blps.dto.vacancy.VacancyResponse
import com.arekalov.blps.exception.NotFoundException
import com.arekalov.blps.exception.ValidationException
import com.arekalov.blps.mapper.toPagedResponse
import com.arekalov.blps.mapper.toResponse
import com.arekalov.blps.model.TariffUsageHistory
import com.arekalov.blps.model.enum.VacancyStatus
import com.arekalov.blps.repository.TariffUsageHistoryRepository
import com.arekalov.blps.repository.UserRepository
import com.arekalov.blps.repository.VacancyRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class ModerationService(
    private val vacancyRepository: VacancyRepository,
    private val userRepository: UserRepository,
    private val tariffUsageHistoryRepository: TariffUsageHistoryRepository,
) {

    fun getPendingVacancies(pageable: Pageable): PagedResponse<VacancyResponse> {
        val vacancies = vacancyRepository.findByStatus(VacancyStatus.PENDING_MODERATION, pageable)
        return vacancies.toPagedResponse { it.toResponse() }
    }

    fun getPendingVacanciesCount(): Long {
        return vacancyRepository.countByStatus(VacancyStatus.PENDING_MODERATION)
    }

    @Transactional
    fun approveVacancy(moderatorId: UUID, vacancyId: UUID): VacancyResponse {
        val moderator = userRepository.findById(moderatorId).orElseThrow {
            NotFoundException("Moderator with id $moderatorId not found")
        }

        val vacancy = vacancyRepository.findById(vacancyId).orElseThrow {
            NotFoundException("Vacancy with id $vacancyId not found")
        }

        if (vacancy.status != VacancyStatus.PENDING_MODERATION) {
            throw ValidationException("Only vacancies with PENDING_MODERATION status can be approved")
        }

        val tariff = vacancy.tariff
            ?: throw ValidationException("Vacancy must have a tariff before approval")

        vacancy.moderator = moderator
        vacancy.moderatedAt = LocalDateTime.now()
        vacancy.status = VacancyStatus.PUBLISHED
        vacancy.publishedAt = LocalDateTime.now()
        vacancy.updatedAt = LocalDateTime.now()

        val publishedVacancy = vacancyRepository.save(vacancy)

        val usageHistory = TariffUsageHistory(
            vacancy = publishedVacancy,
            tariff = tariff,
            employer = vacancy.employer,
            moderator = moderator,
            price = tariff.price,
            durationDays = tariff.durationDays,
            publishedAt = publishedVacancy.publishedAt
                ?: throw IllegalStateException("Published vacancy must have publishedAt"),
        )
        tariffUsageHistoryRepository.save(usageHistory)

        return publishedVacancy.toResponse()
    }

    @Transactional
    fun rejectVacancy(moderatorId: UUID, vacancyId: UUID, reason: String): VacancyResponse {
        val moderator = userRepository.findById(moderatorId).orElseThrow {
            NotFoundException("Moderator with id $moderatorId not found")
        }

        val vacancy = vacancyRepository.findById(vacancyId).orElseThrow {
            NotFoundException("Vacancy with id $vacancyId not found")
        }

        if (vacancy.status != VacancyStatus.PENDING_MODERATION) {
            throw ValidationException("Only vacancies with PENDING_MODERATION status can be rejected")
        }

        if (reason.isBlank()) {
            throw ValidationException("Rejection reason cannot be blank")
        }

        vacancy.status = VacancyStatus.REJECTED
        vacancy.moderator = moderator
        vacancy.moderatedAt = LocalDateTime.now()
        vacancy.rejectionReason = reason
        vacancy.updatedAt = LocalDateTime.now()

        val rejectedVacancy = vacancyRepository.save(vacancy)
        return rejectedVacancy.toResponse()
    }
}
