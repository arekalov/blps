package com.arekalov.blps.controller

import com.arekalov.blps.dto.common.ErrorResponse
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
@Tag(name = "User & Admin", description = "User profile and admin endpoints")
class UserController(
    private val userService: UserService,
) {

    @GetMapping
    @SecurityRequirement(name = "basicAuth")
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
        val actorUserId = getCurrentUserId(authentication)
            ?: throw UnauthorizedException("Authentication required")
        val actorRole = getCurrentUserRole(authentication)
        return userService.getUsers(my, actorUserId, actorRole)
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "basicAuth")
    @Operation(
        summary = "Update user",
        description = "Employer can update only their own profile (except role). " +
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
        return userService.getUserById(id)
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "basicAuth")
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
        userService.deleteUser(userId)
    }
}
