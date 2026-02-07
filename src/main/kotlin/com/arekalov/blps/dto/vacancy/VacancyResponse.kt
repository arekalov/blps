package com.arekalov.blps.dto.vacancy

import com.arekalov.blps.model.enum.EmploymentFormat
import com.arekalov.blps.model.enum.EmploymentType
import com.arekalov.blps.model.enum.ExperienceLevel
import com.arekalov.blps.model.enum.VacancyStatus
import com.arekalov.blps.model.enum.WorkFormat
import com.arekalov.blps.model.enum.WorkSchedule
import java.math.BigDecimal
import java.time.LocalDateTime

data class VacancyResponse(
    val id: String,
    val title: String,
    val description: String,
    val experienceLevel: ExperienceLevel,
    val salaryFrom: BigDecimal?,
    val salaryTo: BigDecimal?,
    val employmentType: EmploymentType,
    val workFormat: WorkFormat,
    val employmentFormat: EmploymentFormat,
    val workSchedule: WorkSchedule,
    val city: String,
    val address: String?,
    val companyDescription: String?,
    val additionalSkills: List<String>,
    val status: VacancyStatus,
    val employerId: String,
    val tariffId: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val publishedAt: LocalDateTime?,
)
