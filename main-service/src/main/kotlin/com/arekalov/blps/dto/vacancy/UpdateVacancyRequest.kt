package com.arekalov.blps.dto.vacancy

import com.arekalov.blps.model.enum.EmploymentFormat
import com.arekalov.blps.model.enum.EmploymentType
import com.arekalov.blps.model.enum.ExperienceLevel
import com.arekalov.blps.model.enum.WorkFormat
import com.arekalov.blps.model.enum.WorkSchedule
import com.arekalov.blps.validation.SalaryRange
import com.arekalov.blps.validation.ValidSalaryRange
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal

@ValidSalaryRange
data class UpdateVacancyRequest(
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String? = null,
    @field:Size(max = 10000, message = "Description must not exceed 10000 characters")
    val description: String? = null,
    val experienceLevel: ExperienceLevel? = null,
    @field:Positive(message = "Salary from must be positive")
    override val salaryFrom: BigDecimal? = null,
    @field:Positive(message = "Salary to must be positive")
    override val salaryTo: BigDecimal? = null,
    val employmentType: EmploymentType? = null,
    val workFormat: WorkFormat? = null,
    val employmentFormat: EmploymentFormat? = null,
    val workSchedule: WorkSchedule? = null,
    @field:Size(max = 100, message = "City must not exceed 100 characters")
    val city: String? = null,
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
        >? = null,
) : SalaryRange
