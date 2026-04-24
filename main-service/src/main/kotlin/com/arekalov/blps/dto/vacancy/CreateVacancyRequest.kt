package com.arekalov.blps.dto.vacancy

import com.arekalov.blps.model.enum.EmploymentFormat
import com.arekalov.blps.model.enum.EmploymentType
import com.arekalov.blps.model.enum.ExperienceLevel
import com.arekalov.blps.model.enum.WorkFormat
import com.arekalov.blps.model.enum.WorkSchedule
import com.arekalov.blps.validation.SalaryRange
import com.arekalov.blps.validation.ValidSalaryRange
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal

@ValidSalaryRange
data class CreateVacancyRequest(
    @field:NotBlank(message = "Title cannot be blank")
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String,
    @field:NotBlank(message = "Description cannot be blank")
    @field:Size(max = 10000, message = "Description must not exceed 10000 characters")
    val description: String,
    @field:NotNull(message = "Experience level cannot be null")
    val experienceLevel: ExperienceLevel,
    @field:Positive(message = "Salary from must be positive")
    override val salaryFrom: BigDecimal? = null,
    @field:Positive(message = "Salary to must be positive")
    override val salaryTo: BigDecimal? = null,
    @field:NotNull(message = "Employment type cannot be null")
    val employmentType: EmploymentType,
    @field:NotNull(message = "Work format cannot be null")
    val workFormat: WorkFormat,
    @field:NotNull(message = "Employment format cannot be null")
    val employmentFormat: EmploymentFormat,
    @field:NotNull(message = "Work schedule cannot be null")
    val workSchedule: WorkSchedule,
    @field:NotBlank(message = "City cannot be blank")
    @field:Size(max = 100, message = "City must not exceed 100 characters")
    val city: String,
    @field:Size(max = 500, message = "Address must not exceed 500 characters")
    val address: String? = null,
    @field:Size(max = 5000, message = "Company description must not exceed 5000 characters")
    val companyDescription: String? = null,
    @field:Size(max = 50, message = "Cannot add more than 50 skills")
    val additionalSkills: List<
        @Size(
            max = 100,
            message = "Skill name must not exceed 100 characters",
        ) String,
        > = emptyList(),
) : SalaryRange
