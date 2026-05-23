package com.arekalov.blps.delegate.user.command

import com.arekalov.blps.dto.user.UpdateUserRequest
import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.camunda.ProcessUserResolver
import com.arekalov.blps.model.enum.UserRole
import com.arekalov.blps.service.UserService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("userUpdateDelegate")
class UserUpdateDelegate(
    private val userService: UserService,
    private val processUserResolver: ProcessUserResolver,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val actor = processUserResolver.resolveActor(execution)
        val actorId = actor.id!!
        val actorRole = processUserResolver.resolveActorRole(execution, actor)

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
        execution.setVariable("resultSummary", "Профиль обновлён.\n\n" + CamundaPresentation.formatUser(updated))
        execution.setVariable("showResult", true)
    }
}
