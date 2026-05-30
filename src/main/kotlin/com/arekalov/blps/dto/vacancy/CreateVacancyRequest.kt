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
    @field:NotBlank(message = "Поле не может быть пустым")
    @field:Size(max = 255, message = "Не более 255 символов")
    val title: String,
    @field:NotBlank(message = "Поле не может быть пустым")
    @field:Size(max = 10000, message = "Не более 10000 символов")
    val description: String,
    @field:NotNull(message = "Выберите значение")
    val experienceLevel: ExperienceLevel,
    @field:Positive(message = "Должно быть положительным числом")
    override val salaryFrom: BigDecimal? = null,
    @field:Positive(message = "Должно быть положительным числом")
    override val salaryTo: BigDecimal? = null,
    @field:NotNull(message = "Выберите значение")
    val employmentType: EmploymentType,
    @field:NotNull(message = "Выберите значение")
    val workFormat: WorkFormat,
    @field:NotNull(message = "Выберите значение")
    val employmentFormat: EmploymentFormat,
    @field:NotNull(message = "Выберите значение")
    val workSchedule: WorkSchedule,
    @field:NotBlank(message = "Поле не может быть пустым")
    @field:Size(max = 100, message = "Не более 100 символов")
    val city: String,
    @field:Size(max = 500, message = "Не более 500 символов")
    val address: String? = null,
    @field:Size(max = 5000, message = "Не более 5000 символов")
    val companyDescription: String? = null,
    @field:Size(max = 50, message = "Не более 50 навыков")
    val additionalSkills: List<
        @Size(
            max = 100,
            message = "Название навыка не более 100 символов",
        ) String,
        > = emptyList(),
) : SalaryRange
