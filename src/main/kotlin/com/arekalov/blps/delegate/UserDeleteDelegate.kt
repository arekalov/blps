package com.arekalov.blps.delegate

import com.arekalov.blps.repository.UserRepository
import com.arekalov.blps.service.UserService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("userDeleteDelegate")
class UserDeleteDelegate(
    private val userService: UserService,
    private val userRepository: UserRepository,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val actorId = resolveActorId(execution)
        val targetUserId = UUID.fromString(execution.getVariable("targetUserId") as String)

        userService.deleteUser(currentUserId = actorId, targetUserId = targetUserId)
        execution.setVariable("deletedUserId", targetUserId.toString())
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
}
