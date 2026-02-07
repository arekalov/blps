package com.arekalov.blps.mapper

import com.arekalov.blps.dto.tariff.CreateTariffRequest
import com.arekalov.blps.dto.tariff.TariffResponse
import com.arekalov.blps.model.Tariff

fun Tariff.toResponse() = TariffResponse(
    id = id.toString(),
    name = name,
    price = price,
    durationDays = durationDays,
    description = description,
)

fun CreateTariffRequest.toEntity() = Tariff(
    name = name,
    price = price,
    durationDays = durationDays,
    description = description,
)
