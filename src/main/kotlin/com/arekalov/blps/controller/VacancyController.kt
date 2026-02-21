package com.arekalov.blps.controller

import com.arekalov.blps.common.validateAndCreatePageable
import com.arekalov.blps.dto.common.ErrorResponse
import com.arekalov.blps.dto.common.PagedResponse
import com.arekalov.blps.dto.vacancy.CreateVacancyRequest
import com.arekalov.blps.dto.vacancy.UpdateVacancyRequest
import com.arekalov.blps.dto.vacancy.VacancyResponse
import com.arekalov.blps.exception.UnauthorizedException
import com.arekalov.blps.model.enum.UserRole
import com.arekalov.blps.model.enum.VacancyStatus
import com.arekalov.blps.service.VacancyService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/vacancies")
@Tag(name = "Vacancies", description = "Vacancy management endpoints")
@Profile
class VacancyController(
    private val vacancyService: VacancyService,
) {

    @GetMapping
    @Operation(
        summary = "Get vacancies",
        description = "Get paginated list of vacancies. Use my=true to get only your vacancies (requires auth)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Vacancies retrieved successfully"),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid token (when my=true)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun getAllVacancies(
        authentication: Authentication?,
        @RequestParam(required = false) status: VacancyStatus?,
        @RequestParam(required = false, defaultValue = "false") my: Boolean,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<VacancyResponse> {
        val pageable = validateAndCreatePageable(
            page = page,
            size = size,
            sort = Sort.by(Sort.Direction.DESC, "createdAt"),
        )

        return if (my) {
            if (authentication == null) {
                throw UnauthorizedException("Authentication required when my=true")
            }
            val userId = authentication.principal as UUID
            vacancyService.getMyVacancies(userId, status, pageable)
        } else {
            vacancyService.getAllVacancies(status, pageable)
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vacancy by ID", description = "Get vacancy details by ID (public)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Vacancy found"),
            ApiResponse(
                responseCode = "404",
                description = "Vacancy not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun getVacancyById(@PathVariable id: UUID): VacancyResponse {
        return vacancyService.getVacancyById(id)
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create vacancy", description = "Create a new vacancy draft")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Vacancy created successfully"),
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
        ],
    )
    fun createVacancy(
        authentication: Authentication,
        @Valid @RequestBody request: CreateVacancyRequest,
    ): VacancyResponse {
        val userId = authentication.principal as UUID
        return vacancyService.createVacancy(userId, request)
    }

    @PatchMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update vacancy", description = "Partially update existing vacancy (owner or admin)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Vacancy updated successfully"),
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
                description = "Forbidden - you don't have permission to update this vacancy",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Vacancy not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun updateVacancy(
        authentication: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateVacancyRequest,
    ): VacancyResponse {
        val userId = authentication.principal as UUID
        val userRole = getUserRole(authentication)
        return vacancyService.updateVacancy(userId, id, userRole, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete vacancy", description = "Delete vacancy (owner or admin)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Vacancy deleted successfully"),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid token",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - you don't have permission to delete this vacancy",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Vacancy not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun deleteVacancy(
        authentication: Authentication,
        @PathVariable id: UUID,
    ) {
        val userId = authentication.principal as UUID
        val userRole = getUserRole(authentication)
        vacancyService.deleteVacancy(userId, id, userRole)
    }

    @PatchMapping("/{id}/tariff")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Select tariff", description = "Select tariff for vacancy (BPMN: Draft -> Payment)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Tariff selected successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Validation error - invalid tariff or vacancy state",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid token",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - you don't have permission to modify this vacancy",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Vacancy or tariff not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun selectTariff(
        authentication: Authentication,
        @PathVariable id: UUID,
        @RequestParam tariffId: UUID,
    ): VacancyResponse {
        val userId = authentication.principal as UUID
        val userRole = getUserRole(authentication)
        return vacancyService.selectTariff(userId, id, tariffId, userRole)
    }

    @PatchMapping("/{id}/publish")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Publish vacancy", description = "Publish vacancy (BPMN: Draft -> Published)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Vacancy published successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Validation error - vacancy must have a tariff selected",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid token",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - you don't have permission to publish this vacancy",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Vacancy not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun publishVacancy(
        authentication: Authentication,
        @PathVariable id: UUID,
    ): VacancyResponse {
        val userId = authentication.principal as UUID
        val userRole = getUserRole(authentication)
        return vacancyService.publishVacancy(userId, id, userRole)
    }

    @PatchMapping("/{id}/archive")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Archive vacancy", description = "Archive vacancy (BPMN: Published -> Archived)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Vacancy archived successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Validation error - vacancy must be published to archive",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid token",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - you don't have permission to archive this vacancy",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Vacancy not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun archiveVacancy(
        authentication: Authentication,
        @PathVariable id: UUID,
    ): VacancyResponse {
        val userId = authentication.principal as UUID
        val userRole = getUserRole(authentication)
        return vacancyService.archiveVacancy(userId, id, userRole)
    }

    private fun getUserRole(authentication: Authentication): UserRole {
        val authorities = authentication.authorities.map { it.authority }
        return when {
            authorities.contains("ROLE_ADMIN") -> UserRole.ADMIN
            authorities.contains("ROLE_EMPLOYER") -> UserRole.EMPLOYER
            else -> UserRole.EMPLOYER
        }
    }
}
