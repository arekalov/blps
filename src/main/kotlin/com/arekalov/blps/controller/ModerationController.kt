package com.arekalov.blps.controller

import com.arekalov.blps.common.validateAndCreatePageable
import com.arekalov.blps.dto.common.ErrorResponse
import com.arekalov.blps.dto.common.PagedResponse
import com.arekalov.blps.dto.moderation.RejectVacancyRequest
import com.arekalov.blps.dto.vacancy.VacancyResponse
import com.arekalov.blps.exception.UnauthorizedException
import com.arekalov.blps.exception.ValidationException
import com.arekalov.blps.model.enum.ModerationAction
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

    @PostMapping("/{vacancyId}/moderate")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    @SecurityRequirement(name = "basicAuth")
    @Operation(
        summary = "[MODERATOR, ADMIN] Moderate vacancy",
        description = "Approve or reject vacancy (moderator and admin). " +
            "Use action=APPROVE to approve and publish, action=REJECT to reject with reason.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Vacancy moderated successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Validation error - vacancy must be in PENDING_MODERATION status, " +
                    "reason required for REJECT action",
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
    @Suppress("SwallowedException")
    fun moderateVacancy(
        authentication: Authentication,
        @PathVariable vacancyId: UUID,
        @RequestParam action: String,
        @Valid @RequestBody(required = false) request: RejectVacancyRequest?,
    ): VacancyResponse {
        val moderatorId = getCurrentUserId(authentication)
            ?: throw UnauthorizedException("Authentication required")

        val moderationAction = try {
            ModerationAction.valueOf(action.uppercase())
        } catch (e: IllegalArgumentException) {
            throw ValidationException("Invalid action: $action. Must be APPROVE or REJECT")
        }

        return when (moderationAction) {
            ModerationAction.APPROVE -> moderationService.approveVacancy(moderatorId, vacancyId)
            ModerationAction.REJECT -> {
                val reason = request?.reason
                    ?: throw ValidationException("Rejection reason is required for REJECT action")
                moderationService.rejectVacancy(moderatorId, vacancyId, reason)
            }
        }
    }
}
