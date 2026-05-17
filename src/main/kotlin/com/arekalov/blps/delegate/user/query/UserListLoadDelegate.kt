package com.arekalov.blps.delegate.user.query

import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.camunda.ProcessUserResolver
import com.arekalov.blps.service.UserService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component("userListLoadDelegate")
class UserListLoadDelegate(
    private val userService: UserService,
    private val processUserResolver: ProcessUserResolver,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val actor = processUserResolver.resolveActor(execution)
        val role = processUserResolver.resolveActorRole(execution, actor)
        val page = CamundaPresentation.intVar(execution, "page", 0)
        val size = CamundaPresentation.intVar(execution, "size", 20)
        val my = CamundaPresentation.boolVar(execution, "my")

        val result = userService.getAllUsers(actor.id!!, role, my, PageRequest.of(page, size))
        execution.setVariable("resultSummary", CamundaPresentation.formatUserList(result))
    }
}
