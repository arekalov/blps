package com.arekalov.blps.service

import com.arekalov.blps.dto.common.PagedResponse
import com.arekalov.blps.dto.tariff.CreateTariffRequest
import com.arekalov.blps.dto.tariff.TariffResponse
import com.arekalov.blps.dto.tariff.UpdateTariffRequest
import com.arekalov.blps.exception.NotFoundException
import com.arekalov.blps.mapper.toEntity
import com.arekalov.blps.mapper.toPagedResponse
import com.arekalov.blps.mapper.toResponse
import com.arekalov.blps.repository.TariffRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TariffService(
    private val tariffRepository: TariffRepository,
) {

    @Transactional(readOnly = true)
    fun getAllTariffs(pageable: Pageable): PagedResponse<TariffResponse> {
        return tariffRepository.findAll(pageable).toPagedResponse { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getTariffById(id: UUID): TariffResponse {
        val tariff = tariffRepository.findById(id).orElseThrow {
            NotFoundException("Tariff with id $id not found")
        }
        return tariff.toResponse()
    }

    @Transactional
    fun createTariff(request: CreateTariffRequest): TariffResponse {
        val tariff = request.toEntity()
        val savedTariff = tariffRepository.save(tariff)
        return savedTariff.toResponse()
    }

    @Transactional
    fun updateTariff(id: UUID, request: UpdateTariffRequest): TariffResponse {
        val tariff = tariffRepository.findById(id).orElseThrow {
            NotFoundException("Tariff with id $id not found")
        }

        request.name?.let { tariff.name = it }
        request.price?.let { tariff.price = it }
        request.durationDays?.let { tariff.durationDays = it }
        request.description?.let { tariff.description = it }

        val updatedTariff = tariffRepository.save(tariff)
        return updatedTariff.toResponse()
    }

    @Transactional
    fun deleteTariff(id: UUID) {
        if (!tariffRepository.existsById(id)) {
            throw NotFoundException("Tariff with id $id not found")
        }
        tariffRepository.deleteById(id)
    }
}
