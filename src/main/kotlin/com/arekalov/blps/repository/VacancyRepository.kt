package com.arekalov.blps.repository

import com.arekalov.blps.model.Vacancy
import com.arekalov.blps.model.enum.VacancyStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VacancyRepository : JpaRepository<Vacancy, UUID> {
    fun findByEmployerId(employerId: UUID): List<Vacancy>
    fun findByStatus(status: VacancyStatus): List<Vacancy>
    fun findByEmployerIdAndStatus(employerId: UUID, status: VacancyStatus): List<Vacancy>
}
