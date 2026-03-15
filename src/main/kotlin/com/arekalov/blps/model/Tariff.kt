package com.arekalov.blps.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "tariffs")
data class Tariff(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false)
    var price: BigDecimal,

    @Column(nullable = false, name = "duration_days")
    var durationDays: Int,

    @Column(columnDefinition = "TEXT")
    var description: String,

    @OneToMany(mappedBy = "tariff", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    val vacancies: MutableList<Vacancy> = mutableListOf(),
)
