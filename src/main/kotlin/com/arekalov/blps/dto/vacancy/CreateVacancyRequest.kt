package com.arekalov.blps.dto.vacancy

import com.arekalov.blps.model.enum.EmploymentFormat
import com.arekalov.blps.model.enum.EmploymentType
import com.arekalov.blps.model.enum.ExperienceLevel
import com.arekalov.blps.model.enum.WorkFormat
import com.arekalov.blps.model.enum.WorkSchedule
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class CreateVacancyRequest(
    @field:NotBlank(message = "Title cannot be blank")
    val title: String,
    @field:NotBlank(message = "Description cannot be blank")
    val description: String,
    @field:NotNull(message = "Experience level cannot be null")
    val experienceLevel: ExperienceLevel,
    @field:Positive(message = "Salary from must be positive")
    val salaryFrom: BigDecimal? = null,
    @field:Positive(message = "Salary to must be positive")
    val salaryTo: BigDecimal? = null,
    @field:NotNull(message = "Employment type cannot be null")
    val employmentType: EmploymentType,
    @field:NotNull(message = "Work format cannot be null")
    val workFormat: WorkFormat,
    @field:NotNull(message = "Employment format cannot be null")
    val employmentFormat: EmploymentFormat,
    @field:NotNull(message = "Work schedule cannot be null")
    val workSchedule: WorkSchedule,
    @field:NotBlank(message = "City cannot be blank")
    val city: String,
    val address: String? = null,
    val companyDescription: String? = null,
    val additionalSkills: List<String> = emptyList(),
)
