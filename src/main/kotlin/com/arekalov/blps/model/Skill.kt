package com.arekalov.blps.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "skills")
data class Skill(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(unique = true, nullable = false)
    val name: String,

    @ManyToMany(mappedBy = "skills")
    val vacancies: MutableSet<Vacancy> = mutableSetOf(),
)
