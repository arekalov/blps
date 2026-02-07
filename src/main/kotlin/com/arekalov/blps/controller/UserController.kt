package com.arekalov.blps.controller

import com.arekalov.blps.dto.user.UserResponse
import com.arekalov.blps.mapper.toResponse
import com.arekalov.blps.repository.UserRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "User & Admin", description = "User profile and admin endpoints")
class UserController(
    private val userRepository: UserRepository,
) {

    @GetMapping("/users/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user profile")
    fun getCurrentUser(authentication: Authentication): UserResponse {
        val userId = UUID.fromString(authentication.principal as String)
        val user = userRepository.findById(userId).orElseThrow {
            throw IllegalStateException("User not found")
        }
        return user.toResponse()
    }

    @DeleteMapping("/admin/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete user",
        description = "Delete user and all their vacancies (admin only, cascaded deletion)",
    )
    fun deleteUser(@PathVariable userId: UUID) {
        userRepository.deleteById(userId)
    }
}
