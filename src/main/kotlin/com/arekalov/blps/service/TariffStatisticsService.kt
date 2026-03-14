package com.arekalov.blps.service

import com.arekalov.blps.dto.common.PagedResponse
import com.arekalov.blps.dto.tariff.TariffStatisticsResponse
import com.arekalov.blps.dto.tariff.TariffUsageHistoryResponse
import com.arekalov.blps.exception.NotFoundException
import com.arekalov.blps.mapper.toPagedResponse
import com.arekalov.blps.model.TariffUsageHistory
import com.arekalov.blps.repository.TariffRepository
import com.arekalov.blps.repository.TariffUsageHistoryRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TariffStatisticsService(
    private val tariffRepository: TariffRepository,
    private val tariffUsageHistoryRepository: TariffUsageHistoryRepository,
) {

    fun getTariffStatistics(tariffId: UUID): TariffStatisticsResponse {
        val tariff = tariffRepository.findById(tariffId).orElseThrow {
            NotFoundException("Tariff with id $tariffId not found")
        }

        val usageCount = tariffUsageHistoryRepository.countByTariffId(tariffId)
        val allHistory = tariffUsageHistoryRepository.findByTariffId(
            tariffId,
            Pageable.unpaged(),
        ).content

        val totalRevenue = allHistory.sumOf { it.price }
        val firstUsedAt = allHistory.minByOrNull { it.publishedAt }?.publishedAt
        val lastUsedAt = allHistory.maxByOrNull { it.publishedAt }?.publishedAt

        return TariffStatisticsResponse(
            tariffId = tariff.id.toString(),
            tariffName = tariff.name,
            currentPrice = tariff.price,
            currentDurationDays = tariff.durationDays,
            usageCount = usageCount,
            totalRevenue = totalRevenue,
            firstUsedAt = firstUsedAt,
            lastUsedAt = lastUsedAt,
        )
    }

    fun getTariffUsageHistory(tariffId: UUID, pageable: Pageable): PagedResponse<TariffUsageHistoryResponse> {
        if (!tariffRepository.existsById(tariffId)) {
            throw NotFoundException("Tariff with id $tariffId not found")
        }

        val history = tariffUsageHistoryRepository.findByTariffId(tariffId, pageable)
        return history.toPagedResponse { it.toResponse() }
    }

    fun getEmployerUsageHistory(
        employerId: UUID,
        pageable: Pageable,
    ): PagedResponse<TariffUsageHistoryResponse> {
        val history = tariffUsageHistoryRepository.findByEmployerId(employerId, pageable)
        return history.toPagedResponse { it.toResponse() }
    }

    fun getModeratorUsageHistory(
        moderatorId: UUID,
        pageable: Pageable,
    ): PagedResponse<TariffUsageHistoryResponse> {
        val history = tariffUsageHistoryRepository.findByModeratorId(moderatorId, pageable)
        return history.toPagedResponse { it.toResponse() }
    }

    private fun TariffUsageHistory.toResponse() = TariffUsageHistoryResponse(
        id = id.toString(),
        vacancyId = vacancy.id.toString(),
        vacancyTitle = vacancy.title,
        tariffId = tariff.id.toString(),
        tariffName = tariff.name,
        employerId = employer.id.toString(),
        employerCompanyName = employer.companyName,
        moderatorId = moderator.id.toString(),
        moderatorEmail = moderator.email,
        price = price,
        durationDays = durationDays,
        publishedAt = publishedAt,
    )
}
