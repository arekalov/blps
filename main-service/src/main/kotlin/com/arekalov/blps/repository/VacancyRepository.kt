package com.arekalov.blps.repository

import com.arekalov.blps.model.Vacancy
import com.arekalov.blps.model.enum.VacancyStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface VacancyRepository : JpaRepository<Vacancy, UUID> {
    fun findByEmployerId(employerId: UUID, pageable: Pageable): Page<Vacancy>
    fun findByEmployerId(employerId: UUID): List<Vacancy>
    fun findByStatus(status: VacancyStatus, pageable: Pageable): Page<Vacancy>
    fun findByEmployerIdAndStatus(employerId: UUID, status: VacancyStatus, pageable: Pageable): Page<Vacancy>
    fun countByStatus(status: VacancyStatus): Long

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
        UPDATE vacancies v
        SET status = 'ARCHIVED', updated_at = :updatedAt
        FROM tariffs t
        WHERE v.tariff_id = t.id
          AND v.status = 'PUBLISHED'
          AND v.published_at IS NOT NULL
          AND v.published_at + (t.duration_days * INTERVAL '1 day') < :asOf
        """,
        nativeQuery = true,
    )
    fun archivePublishedExpiredBefore(
        @Param("asOf") asOf: LocalDateTime,
        @Param("updatedAt") updatedAt: LocalDateTime,
    ): Int
}
