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

    @Column(nullable = false, length = 255)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, name = "experience_level")
    var experienceLevel: ExperienceLevel,

    @Column(name = "salary_from")
    var salaryFrom: BigDecimal? = null,

    @Column(name = "salary_to")
    var salaryTo: BigDecimal? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, name = "employment_type")
    var employmentType: EmploymentType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, name = "work_format")
    var workFormat: WorkFormat,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, name = "employment_format")
    var employmentFormat: EmploymentFormat,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, name = "work_schedule")
    var workSchedule: WorkSchedule,

    @Column(nullable = false, length = 100)
    var city: String,

    @Column(length = 500)
    var address: String? = null,

    @Column(columnDefinition = "TEXT", name = "company_description")
    var companyDescription: String? = null,

    @ManyToMany
    @JoinTable(
        name = "vacancy_skills",
        joinColumns = [JoinColumn(name = "vacancy_id")],
        inverseJoinColumns = [JoinColumn(name = "skill_id")],
    )
    var additionalSkills: MutableList<Skill> = mutableListOf(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var status: VacancyStatus = VacancyStatus.DRAFT,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    val employer: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id")
    var tariff: Tariff? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    var moderator: User? = null,

    @Column(name = "moderated_at")
    var moderatedAt: LocalDateTime? = null,

    @Column(columnDefinition = "TEXT", name = "rejection_reason")
    var rejectionReason: String? = null,

    @Column(nullable = false, updatable = false, name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false, name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "published_at")
    var publishedAt: LocalDateTime? = null,
)
