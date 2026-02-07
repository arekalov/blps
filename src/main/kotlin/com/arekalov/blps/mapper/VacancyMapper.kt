package com.arekalov.blps.mapper

import com.arekalov.blps.dto.vacancy.CreateVacancyRequest
import com.arekalov.blps.dto.vacancy.VacancyResponse
import com.arekalov.blps.model.Skill
import com.arekalov.blps.model.User
import com.arekalov.blps.model.Vacancy
import com.arekalov.blps.model.enum.VacancyStatus
import java.time.LocalDateTime

fun Vacancy.toResponse() = VacancyResponse(
    id = id.toString(),
    title = title,
    description = description,
    experienceLevel = experienceLevel,
    salaryFrom = salaryFrom,
    salaryTo = salaryTo,
    employmentType = employmentType,
    workFormat = workFormat,
    employmentFormat = employmentFormat,
    workSchedule = workSchedule,
    city = city,
    address = address,
    companyDescription = companyDescription,
    additionalSkills = additionalSkills.map { it.name },
    status = status,
    employerId = employer.id.toString(),
    tariffId = tariff?.id?.toString(),
    createdAt = createdAt,
    updatedAt = updatedAt,
    publishedAt = publishedAt,
)

fun CreateVacancyRequest.toEntity(employer: User, skills: List<Skill>) = Vacancy(
    title = title,
    description = description,
    experienceLevel = experienceLevel,
    salaryFrom = salaryFrom,
    salaryTo = salaryTo,
    employmentType = employmentType,
    workFormat = workFormat,
    employmentFormat = employmentFormat,
    workSchedule = workSchedule,
    city = city,
    address = address,
    companyDescription = companyDescription,
    additionalSkills = skills.toMutableList(),
    status = VacancyStatus.DRAFT,
    employer = employer,
    tariff = null,
    createdAt = LocalDateTime.now(),
    updatedAt = LocalDateTime.now(),
    publishedAt = null,
)
