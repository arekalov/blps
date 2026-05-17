package com.arekalov.blps.camunda

import com.arekalov.blps.exception.NotFoundException
import com.arekalov.blps.model.User
import com.arekalov.blps.model.enum.UserRole
import com.arekalov.blps.repository.UserRepository
import org.camunda.bpm.engine.IdentityService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ProcessUserResolver(
    private val userRepository: UserRepository,
    @Lazy private val identityService: IdentityService,
) {

    fun resolveActor(execution: DelegateExecution): User {
        (execution.getVariable("initiatorUserId") as? String)?.takeIf { it.isNotBlank() }?.let { id ->
            return userRepository.findById(UUID.fromString(id)).orElseThrow {
                NotFoundException("User with id $id not found")
            }
        }

        val email = (execution.getVariable("initiatorEmail") as? String)?.takeIf { it.isNotBlank() }
            ?: identityService.currentAuthentication?.userId
            ?: throw IllegalStateException(
                "Cannot resolve actor: log in to Tasklist or set initiatorEmail process variable",
            )

        return userRepository.findByEmail(email)
            ?: throw NotFoundException("User with email $email not found")
    }

    fun resolveActorOrNull(execution: DelegateExecution): User? =
        runCatching { resolveActor(execution) }.getOrNull()

    fun resolveActorRole(execution: DelegateExecution, actor: User): UserRole {
        val roleStr = execution.getVariable("actorRole") as? String
        if (!roleStr.isNullOrBlank()) {
            return runCatching { UserRole.valueOf(roleStr) }.getOrDefault(actor.role)
        }
        return actor.role
    }

    fun enrichExecution(execution: DelegateExecution) {
        val user = resolveActor(execution)
        execution.setVariable("initiatorUserId", user.id.toString())
        execution.setVariable("initiatorEmail", user.email)
        execution.setVariable("actorUserId", user.id.toString())
        execution.setVariable("actorRole", user.role.name)
    }
}
