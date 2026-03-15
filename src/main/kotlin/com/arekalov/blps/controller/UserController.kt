package com.arekalov.blps.controller

import com.arekalov.blps.common.validateAndCreatePageable
import com.arekalov.blps.dto.common.ErrorResponse
import com.arekalov.blps.dto.common.PagedResponse
import com.arekalov.blps.dto.user.UpdateUserRequest
import com.arekalov.blps.dto.user.UserResponse
import com.arekalov.blps.exception.UnauthorizedException
import com.arekalov.blps.security.getCurrentUserId
import com.arekalov.blps.security.getCurrentUserRole
import com.arekalov.blps.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User profile and admin endpoints")
class UserController(
    private val userService: UserService,
) {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "basicAuth")
    @Operation(
        summary = "[EMPLOYER/MODERATOR/ADMIN] Get users",
        description = "Get paginated list of all users (admin only when my=false) or current user (when my=true)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid credentials",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun getAllUsers(
        authentication: Authentication,
        @RequestParam(required = false, defaultValue = "false") my: Boolean,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<UserResponse> {
        val userId = getCurrentUserId(authentication)
            ?: throw UnauthorizedException("Authentication required")
        val userRole = getCurrentUserRole(authentication)
        val pageable = validateAndCreatePageable(page, size)
        return userService.getAllUsers(userId, userRole, my, pageable)
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "basicAuth")
    @Operation(
        summary = "[EMPLOYER/MODERATOR/ADMIN] Get current user",
        description = "Get current authenticated user profile",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Current user retrieved successfully"),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid credentials",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun getCurrentUser(authentication: Authentication): UserResponse {
        val userId = getCurrentUserId(authentication)
            ?: throw UnauthorizedException("Authentication required")
        return userService.getUserById(userId)
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "basicAuth")
    @Operation(
        summary = "[OWNER, ADMIN] Update user",
        description = "Employer/Moderator can update only their own profile (except role). " +
            "Admin can update any user and change role.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User updated successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Validation error - invalid request data or email already exists",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid token",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - employer cannot update another user or change role",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    fun updateUser(
        authentication: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateUserRequest,
    ): UserResponse {
        val actorUserId = getCurrentUserId(authentication)
            ?: throw UnauthorizedException("Authentication required")
        val actorRole = getCurrentUserRole(authentication)
        return userService.updateUser(actorUserId, actorRole, id, request)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "[PUBLIC] Get user by ID",
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
        return userService.getUserById(id)
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "basicAuth")
    @Operation(
        summary = "[ADMIN] Delete user",
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
        userService.deleteUser(userId)
    }
}
