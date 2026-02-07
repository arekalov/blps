package com.arekalov.blps.dto.vacancy

import com.arekalov.blps.model.enum.EmploymentFormat
import com.arekalov.blps.model.enum.EmploymentType
import com.arekalov.blps.model.enum.ExperienceLevel
import com.arekalov.blps.model.enum.WorkFormat
import com.arekalov.blps.model.enum.WorkSchedule
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class UpdateVacancyRequest(
    val title: String? = null,
    val description: String? = null,
    val experienceLevel: ExperienceLevel? = null,
    @field:Positive(message = "Salary from must be positive")
    val salaryFrom: BigDecimal? = null,
    @field:Positive(message = "Salary to must be positive")
    val salaryTo: BigDecimal? = null,
    val employmentType: EmploymentType? = null,
    val workFormat: WorkFormat? = null,
    val employmentFormat: EmploymentFormat? = null,
    val workSchedule: WorkSchedule? = null,
    val city: String? = null,
    val address: String? = null,
    val companyDescription: String? = null,
    val additionalSkills: List<String>? = null,
)
