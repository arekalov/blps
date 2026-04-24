package com.arekalov.blps.repository

import com.arekalov.blps.model.Tariff
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TariffRepository : JpaRepository<Tariff, UUID>
