package com.arekalov.blps.delegate.user.command

import com.arekalov.blps.camunda.ProcessUserResolver
import com.arekalov.blps.service.UserService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("userDeleteDelegate")
class UserDeleteDelegate(
    private val userService: UserService,
    private val processUserResolver: ProcessUserResolver,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val actorId = processUserResolver.resolveActor(execution).id!!
        val targetUserId = UUID.fromString(execution.getVariable("targetUserId") as String)

        userService.deleteUser(currentUserId = actorId, targetUserId = targetUserId)
        execution.setVariable("deletedUserId", targetUserId.toString())
    }
}
