package com.arekalov.blps.controller

import com.arekalov.blps.common.PaginationConstants.DEFAULT_PAGE_SIZE
import com.arekalov.blps.dto.common.ErrorResponse
import com.arekalov.blps.dto.common.PagedResponse
import com.arekalov.blps.dto.tariff.CreateTariffRequest
import com.arekalov.blps.dto.tariff.TariffResponse
import com.arekalov.blps.dto.tariff.UpdateTariffRequest
import com.arekalov.blps.service.TariffService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
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
        @PageableDefault(
            size = DEFAULT_PAGE_SIZE,
            sort = ["price"],
            direction = Sort.Direction.ASC,
        ) pageable: Pageable,
    ): PagedResponse<TariffResponse> {
        return tariffService.getAllTariffs(pageable)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tariff by ID", description = "Get tariff details by ID (public)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Tariff found"),
            ApiResponse(
                responseCode = "404",
                description = "Tariff not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun getTariffById(@PathVariable id: UUID): TariffResponse {
        return tariffService.getTariffById(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create tariff", description = "Create a new tariff (admin only)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Tariff created successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Validation error - invalid request data",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid token",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - admin role required",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun createTariff(@Valid @RequestBody request: CreateTariffRequest): TariffResponse {
        return tariffService.createTariff(request)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update tariff", description = "Update existing tariff (admin only)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Tariff updated successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Validation error - invalid request data",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid token",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - admin role required",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Tariff not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
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
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Tariff deleted successfully"),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid token",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - admin role required",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Tariff not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun deleteTariff(@PathVariable id: UUID) {
        tariffService.deleteTariff(id)
    }
}
