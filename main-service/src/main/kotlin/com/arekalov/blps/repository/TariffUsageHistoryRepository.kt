package com.arekalov.blps.repository

import com.arekalov.blps.model.TariffUsageHistory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TariffUsageHistoryRepository : JpaRepository<TariffUsageHistory, UUID> {
    fun findByTariffId(tariffId: UUID, pageable: Pageable): Page<TariffUsageHistory>
    fun findByEmployerId(employerId: UUID, pageable: Pageable): Page<TariffUsageHistory>
    fun findByModeratorId(moderatorId: UUID, pageable: Pageable): Page<TariffUsageHistory>
    fun countByTariffId(tariffId: UUID): Long
}
