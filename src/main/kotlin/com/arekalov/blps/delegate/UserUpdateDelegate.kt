package com.arekalov.blps.delegate

import com.arekalov.blps.dto.user.UpdateUserRequest
import com.arekalov.blps.model.enum.UserRole
import com.arekalov.blps.repository.UserRepository
import com.arekalov.blps.service.UserService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("userUpdateDelegate")
class UserUpdateDelegate(
    private val userService: UserService,
    private val userRepository: UserRepository,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val actorId = resolveActorId(execution)
        val actorRole = resolveActorRole(execution)

        val targetUserIdStr = execution.getVariable("targetUserId") as? String
        val targetUserId = if (!targetUserIdStr.isNullOrBlank()) {
            UUID.fromString(targetUserIdStr)
        } else {
            actorId
        }

        val newRoleStr = execution.getVariable("newRole") as? String
        val newRole = if (!newRoleStr.isNullOrBlank()) {
            runCatching { UserRole.valueOf(newRoleStr) }.getOrNull()
        } else {
            null
        }

        val request = UpdateUserRequest(
            email = (execution.getVariable("newEmail") as? String)?.takeIf { it.isNotBlank() },
            password = (execution.getVariable("newPassword") as? String)?.takeIf { it.isNotBlank() },
            companyName = (execution.getVariable("newCompanyName") as? String)?.takeIf { it.isNotBlank() },
            role = newRole,
        )

        val updated = userService.updateUser(actorId, actorRole, targetUserId, request)
        execution.setVariable("updatedUserId", updated.id.toString())
    }

    private fun resolveActorId(execution: DelegateExecution): UUID {
        val actorIdStr = execution.getVariable("actorUserId") as? String
            ?: execution.getVariable("initiatorUserId") as? String
        if (!actorIdStr.isNullOrBlank()) return UUID.fromString(actorIdStr)

        val email = execution.getVariable("initiatorEmail") as? String
            ?: throw IllegalStateException("actorUserId or initiatorEmail variable required")
        return userRepository.findByEmail(email)?.id
            ?: throw IllegalStateException("User not found by email: $email")
    }

    private fun resolveActorRole(execution: DelegateExecution): UserRole {
        val roleStr = execution.getVariable("actorRole") as? String ?: return UserRole.EMPLOYER
        return runCatching { UserRole.valueOf(roleStr) }.getOrDefault(UserRole.EMPLOYER)
    }
}
