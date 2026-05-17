package com.arekalov.blps.delegate.user.query

import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.service.UserService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("userViewLoadDelegate")
class UserViewLoadDelegate(
    private val userService: UserService,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val userId = UUID.fromString(execution.getVariable("targetUserId") as String)
        val user = userService.getUserById(userId)
        execution.setVariable("resultSummary", CamundaPresentation.formatUser(user))
    }
}
