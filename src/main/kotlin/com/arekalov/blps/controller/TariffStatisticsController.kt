package com.arekalov.blps.controller

import com.arekalov.blps.common.validateAndCreatePageable
import com.arekalov.blps.dto.common.ErrorResponse
import com.arekalov.blps.dto.common.PagedResponse
import com.arekalov.blps.dto.tariff.TariffStatisticsResponse
import com.arekalov.blps.dto.tariff.TariffUsageHistoryResponse
import com.arekalov.blps.service.TariffStatisticsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/tariffs")
@Tag(name = "Tariff Statistics", description = "Tariff usage statistics and history endpoints")
class TariffStatisticsController(
    private val tariffStatisticsService: TariffStatisticsService,
) {

    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @SecurityRequirement(name = "basicAuth")
    @Operation(
        summary = "[ADMIN, MODERATOR] Get tariff statistics",
        description = "Get aggregated statistics for a tariff (admin/moderator only)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid credentials",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - admin or moderator role required",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Tariff not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun getTariffStatistics(@PathVariable id: UUID): TariffStatisticsResponse {
        return tariffStatisticsService.getTariffStatistics(id)
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @SecurityRequirement(name = "basicAuth")
    @Operation(
        summary = "[ADMIN, MODERATOR] Get tariff usage history",
        description = "Get detailed usage history for a tariff (admin/moderator only)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Usage history retrieved successfully"),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid credentials",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - admin or moderator role required",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Tariff not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun getTariffUsageHistory(
        @PathVariable id: UUID,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<TariffUsageHistoryResponse> {
        val pageable = validateAndCreatePageable(
            page = page,
            size = size,
            sort = Sort.by(Sort.Direction.DESC, "publishedAt"),
        )
        return tariffStatisticsService.getTariffUsageHistory(id, pageable)
    }
}
