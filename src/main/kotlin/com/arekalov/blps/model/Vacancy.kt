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
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "vacancies")
data class Vacancy(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var experienceLevel: ExperienceLevel,

    var salaryFrom: BigDecimal? = null,

    var salaryTo: BigDecimal? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var employmentType: EmploymentType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var workFormat: WorkFormat,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var employmentFormat: EmploymentFormat,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var workSchedule: WorkSchedule,

    @Column(nullable = false)
    var city: String,

    var address: String? = null,

    @Column(columnDefinition = "TEXT")
    var companyDescription: String? = null,

    @ManyToMany
    @JoinTable(
        name = "vacancy_skills",
        joinColumns = [JoinColumn(name = "vacancy_id")],
        inverseJoinColumns = [JoinColumn(name = "skill_id")],
    )
    var additionalSkills: MutableList<Skill> = mutableListOf(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: VacancyStatus = VacancyStatus.DRAFT,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    val employer: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id")
    var tariff: Tariff? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    var publishedAt: LocalDateTime? = null,
)
