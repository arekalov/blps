package com.arekalov.blps.controller

import com.arekalov.blps.common.validateAndCreatePageable
import com.arekalov.blps.dto.common.ErrorResponse
import com.arekalov.blps.dto.common.PagedResponse
import com.arekalov.blps.dto.moderation.RejectVacancyRequest
import com.arekalov.blps.dto.vacancy.VacancyResponse
import com.arekalov.blps.exception.UnauthorizedException
import com.arekalov.blps.security.getCurrentUserId
import com.arekalov.blps.service.ModerationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/moderation")
@Tag(name = "Moderation", description = "Vacancy moderation endpoints (moderator and admin)")
class ModerationController(
    private val moderationService: ModerationService,
) {

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    @SecurityRequirement(name = "basicAuth")
    @Operation(
        summary = "[MODERATOR, ADMIN] Get pending vacancies",
        description = "Get paginated list of vacancies awaiting moderation (moderator and admin)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Pending vacancies retrieved successfully"),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid credentials",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - moderator role required",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun getPendingVacancies(
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<VacancyResponse> {
        val pageable = validateAndCreatePageable(
            page = page,
            size = size,
            sort = Sort.by(Sort.Direction.DESC, "createdAt"),
        )
        return moderationService.getPendingVacancies(pageable)
    }

    @PostMapping("/{vacancyId}/approve")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    @SecurityRequirement(name = "basicAuth")
    @Operation(
        summary = "[MODERATOR, ADMIN] Approve vacancy",
        description = "Approve vacancy and publish it (moderator and admin). Creates tariff usage history record.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Vacancy approved and published successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Validation error - vacancy must be in PENDING_MODERATION status and have a tariff",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid credentials",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - moderator role required",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Vacancy not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun approveVacancy(
        authentication: Authentication,
        @PathVariable vacancyId: UUID,
    ): VacancyResponse {
        val moderatorId = getCurrentUserId(authentication)
            ?: throw UnauthorizedException("Authentication required")
        return moderationService.approveVacancy(moderatorId, vacancyId)
    }

    @PostMapping("/{vacancyId}/reject")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    @SecurityRequirement(name = "basicAuth")
    @Operation(
        summary = "[MODERATOR, ADMIN] Reject vacancy",
        description = "Reject vacancy with a reason (moderator and admin). Vacancy status will be set to REJECTED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Vacancy rejected successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Validation error - vacancy must be in PENDING_MODERATION status and reason is required",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid credentials",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - moderator role required",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Vacancy not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun rejectVacancy(
        authentication: Authentication,
        @PathVariable vacancyId: UUID,
        @Valid @RequestBody request: RejectVacancyRequest,
    ): VacancyResponse {
        val moderatorId = getCurrentUserId(authentication)
            ?: throw UnauthorizedException("Authentication required")
        return moderationService.rejectVacancy(moderatorId, vacancyId, request.reason)
    }
}
