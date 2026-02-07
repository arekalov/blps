package com.arekalov.blps.model

import com.arekalov.blps.model.enum.EmploymentFormat
import com.arekalov.blps.model.enum.EmploymentType
import com.arekalov.blps.model.enum.ExperienceLevel
import com.arekalov.blps.model.enum.VacancyStatus
import com.arekalov.blps.model.enum.WorkFormat
import com.arekalov.blps.model.enum.WorkSchedule
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "vacancies")
data class Vacancy(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val specialization: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val experienceLevel: ExperienceLevel,

    @Column(nullable = false, columnDefinition = "TEXT")
    val workConditions: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val employmentType: EmploymentType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val workSchedule: WorkSchedule,

    @Column(nullable = false)
    val city: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val description: String,

    @Enumerated(EnumType.STRING)
    val workFormat: WorkFormat? = null,

    @Enumerated(EnumType.STRING)
    val employmentFormat: EmploymentFormat? = null,

    val salaryFrom: Int? = null,

    val salaryTo: Int? = null,

    @ManyToMany
    @JoinTable(
        name = "vacancy_skills",
        joinColumns = [JoinColumn(name = "vacancy_id")],
        inverseJoinColumns = [JoinColumn(name = "skill_id")],
    )
    val skills: MutableSet<Skill> = mutableSetOf(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: VacancyStatus = VacancyStatus.DRAFT,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    val employer: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id")
    val tariff: Tariff? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    val publishedAt: LocalDateTime? = null,
)
