package com.arekalov.blps.controller

import com.arekalov.blps.common.PaginationConstants.DEFAULT_PAGE_SIZE
import com.arekalov.blps.dto.common.PagedResponse
import com.arekalov.blps.dto.vacancy.CreateVacancyRequest
import com.arekalov.blps.dto.vacancy.UpdateVacancyRequest
import com.arekalov.blps.dto.vacancy.VacancyResponse
import com.arekalov.blps.model.enum.UserRole
import com.arekalov.blps.model.enum.VacancyStatus
import com.arekalov.blps.service.VacancyService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/vacancies")
@Tag(name = "Vacancies", description = "Vacancy management endpoints")
class VacancyController(
    private val vacancyService: VacancyService,
) {

    @GetMapping
    @Operation(summary = "Get all vacancies", description = "Get paginated list of all vacancies (public)")
    fun getAllVacancies(
        @RequestParam(required = false) status: VacancyStatus?,
        @PageableDefault(
            size = DEFAULT_PAGE_SIZE,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC,
        ) pageable: Pageable,
    ): PagedResponse<VacancyResponse> {
        return vacancyService.getAllVacancies(status, pageable)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vacancy by ID", description = "Get vacancy details by ID (public)")
    fun getVacancyById(@PathVariable id: UUID): VacancyResponse {
        return vacancyService.getVacancyById(id)
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my vacancies", description = "Get paginated list of current user's vacancies")
    fun getMyVacancies(
        authentication: Authentication,
        @RequestParam(required = false) status: VacancyStatus?,
        @PageableDefault(
            size = DEFAULT_PAGE_SIZE,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC,
        ) pageable: Pageable,
    ): PagedResponse<VacancyResponse> {
        val userId = authentication.principal as UUID
        return vacancyService.getMyVacancies(userId, status, pageable)
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create vacancy", description = "Create a new vacancy draft")
    fun createVacancy(
        authentication: Authentication,
        @Valid @RequestBody request: CreateVacancyRequest,
    ): VacancyResponse {
        val userId = authentication.principal as UUID
        return vacancyService.createVacancy(userId, request)
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update vacancy", description = "Update existing vacancy (owner or admin)")
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
