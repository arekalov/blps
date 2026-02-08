package com.arekalov.blps.repository

import com.arekalov.blps.model.Vacancy
import com.arekalov.blps.model.enum.VacancyStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VacancyRepository : JpaRepository<Vacancy, UUID> {
    fun findByEmployerId(employerId: UUID, pageable: Pageable): Page<Vacancy>
    fun findByEmployerId(employerId: UUID): List<Vacancy>
    fun findByStatus(status: VacancyStatus, pageable: Pageable): Page<Vacancy>
    fun findByEmployerIdAndStatus(employerId: UUID, status: VacancyStatus, pageable: Pageable): Page<Vacancy>
}
