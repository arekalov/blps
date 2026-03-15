package com.arekalov.blps.service

import com.arekalov.blps.dto.common.PagedResponse
import com.arekalov.blps.dto.user.UpdateUserRequest
import com.arekalov.blps.dto.user.UserResponse
import com.arekalov.blps.exception.ForbiddenException
import com.arekalov.blps.exception.NotFoundException
import com.arekalov.blps.exception.ValidationException
import com.arekalov.blps.mapper.toPagedResponse
import com.arekalov.blps.mapper.toResponse
import com.arekalov.blps.model.User
import com.arekalov.blps.model.enum.UserRole
import com.arekalov.blps.repository.UserRepository
import com.arekalov.blps.repository.VacancyRepository
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val vacancyRepository: VacancyRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    fun getAllUsers(userId: UUID, userRole: UserRole, my: Boolean, pageable: Pageable): PagedResponse<UserResponse> {
        if (my) {
            val user = userRepository.findById(userId).orElseThrow {
                NotFoundException("User with id $userId not found")
            }
            return PagedResponse(
                content = listOf(user.toResponse()),
                page = 0,
                size = 1,
                totalElements = 1,
                totalPages = 1,
            )
        }

        return when (userRole) {
            UserRole.ADMIN -> {
                val page = userRepository.findAll(pageable)
                page.toPagedResponse { it.toResponse() }
            }
            UserRole.EMPLOYER, UserRole.MODERATOR -> {
                val user = userRepository.findById(userId).orElseThrow {
                    NotFoundException("User with id $userId not found")
                }
                PagedResponse(
                    content = listOf(user.toResponse()),
                    page = 0,
                    size = 1,
                    totalElements = 1,
                    totalPages = 1,
                )
            }
        }
    }

    fun getUserById(id: UUID): UserResponse {
        val user = userRepository.findById(id).orElseThrow {
            NotFoundException("User with id $id not found")
        }
        return user.toResponse()
    }

    @Transactional
    fun updateUser(
        actorUserId: UUID,
        actorRole: UserRole,
        targetUserId: UUID,
        request: UpdateUserRequest,
    ): UserResponse {
        val user = userRepository.findById(targetUserId).orElseThrow {
            NotFoundException("User with id $targetUserId not found")
        }

        validateUpdatePermissions(actorUserId, actorRole, targetUserId, request)

        val newEmail = request.email ?: user.email
        if (newEmail != user.email && userRepository.existsByEmail(newEmail)) {
            throw ValidationException("User with email $newEmail already exists")
        }

        val newPasswordHash = request.password?.let { passwordEncoder.encode(it) } ?: user.passwordHash
        val newCompanyName = request.companyName ?: user.companyName
        val newRole = request.role ?: user.role

        val updated = User(
            id = user.id,
            email = newEmail,
            passwordHash = newPasswordHash,
            companyName = newCompanyName,
            role = newRole,
            createdAt = user.createdAt,
            vacancies = user.vacancies,
        )
        val saved = userRepository.save(updated)
        return saved.toResponse()
    }

    private fun validateUpdatePermissions(
        actorUserId: UUID,
        actorRole: UserRole,
        targetUserId: UUID,
        request: UpdateUserRequest,
    ) {
        when (actorRole) {
            UserRole.EMPLOYER, UserRole.MODERATOR -> {
                if (actorUserId != targetUserId) {
                    throw ForbiddenException("You can only update your own profile")
                }
                if (request.role != null) {
                    throw ForbiddenException("You cannot change your role")
                }
            }
            UserRole.ADMIN -> {}
        }
    }

    @Transactional
    fun deleteUser(currentUserId: UUID, targetUserId: UUID) {
        if (currentUserId == targetUserId) {
            throw ValidationException("You cannot delete yourself")
        }

        val user = userRepository.findById(targetUserId).orElseThrow {
            NotFoundException("User with id $targetUserId not found")
        }
        vacancyRepository.deleteAll(vacancyRepository.findByEmployerId(user.id!!))
        userRepository.delete(user)
    }
}
