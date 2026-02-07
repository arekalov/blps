package com.arekalov.blps.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "skills")
data class Skill(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(unique = true, nullable = false)
    val name: String,

    @ManyToMany(mappedBy = "additionalSkills")
    val vacancies: MutableList<Vacancy> = mutableListOf(),
)
