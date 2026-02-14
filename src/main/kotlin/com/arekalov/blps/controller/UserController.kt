package com.arekalov.blps.controller

import com.arekalov.blps.dto.common.ErrorResponse
import com.arekalov.blps.dto.user.UserResponse
import com.arekalov.blps.exception.ForbiddenException
import com.arekalov.blps.exception.NotFoundException
import com.arekalov.blps.mapper.toResponse
import com.arekalov.blps.repository.UserRepository
import com.arekalov.blps.repository.VacancyRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User & Admin", description = "User profile and admin endpoints")
class UserController(
    private val userRepository: UserRepository,
    private val vacancyRepository: VacancyRepository,
) {

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Get users",
        description = "Get all users (admin only) or current user profile (my=true for any authenticated user)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid token",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - admin role required (when my=false)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun getUsers(
        authentication: Authentication,
        @RequestParam(required = false, defaultValue = "false") my: Boolean,
    ): List<UserResponse> {
        if (my) {
            val userId = authentication.principal as UUID
            val user = userRepository.findById(userId).orElseThrow {
                NotFoundException("User with id $userId not found")
            }
            return listOf(user.toResponse())
        } else {
            val authorities = authentication.authorities.map { it.authority }
            if (!authorities.contains("ROLE_ADMIN")) {
                throw ForbiddenException(
                    "Admin role required to view all users. Use my=true to get your profile",
                )
            }
            return userRepository.findAll().map { it.toResponse() }
        }
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get user by ID",
        description = "Get user profile by ID (public)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User found"),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun getUserById(@PathVariable id: UUID): UserResponse {
        val user = userRepository.findById(id).orElseThrow {
            NotFoundException("User with id $id not found")
        }
        return user.toResponse()
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Delete user",
        description = "Delete user and all their vacancies (admin only, cascaded deletion)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "User deleted successfully"),
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
                description = "User not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun deleteUser(@PathVariable userId: UUID) {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        vacancyRepository.deleteAll(vacancyRepository.findByEmployerId(user.id!!))
        userRepository.delete(user)
    }
}
