package com.arekalov.blps.controller

import com.arekalov.blps.dto.tariff.CreateTariffRequest
import com.arekalov.blps.dto.tariff.TariffResponse
import com.arekalov.blps.dto.tariff.UpdateTariffRequest
import com.arekalov.blps.service.TariffService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/tariffs")
@Tag(name = "Tariffs", description = "Tariff management endpoints")
class TariffController(
    private val tariffService: TariffService,
) {

    @GetMapping
    @Operation(summary = "Get all tariffs", description = "Get paginated list of all tariffs (public)")
    fun getAllTariffs(
        @PageableDefault(size = 20, sort = ["price"], direction = Sort.Direction.ASC) pageable: Pageable,
    ): Page<TariffResponse> {
        return tariffService.getAllTariffs(pageable)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tariff by ID", description = "Get tariff details by ID (public)")
    fun getTariffById(@PathVariable id: UUID): TariffResponse {
        return tariffService.getTariffById(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create tariff", description = "Create a new tariff (admin only)")
    fun createTariff(@Valid @RequestBody request: CreateTariffRequest): TariffResponse {
        return tariffService.createTariff(request)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update tariff", description = "Update existing tariff (admin only)")
    fun updateTariff(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateTariffRequest,
    ): TariffResponse {
        return tariffService.updateTariff(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete tariff", description = "Delete tariff by ID (admin only)")
    fun deleteTariff(@PathVariable id: UUID) {
        tariffService.deleteTariff(id)
    }
}
