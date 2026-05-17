package com.arekalov.blps.delegate.user.query

import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.camunda.ProcessUserResolver
import com.arekalov.blps.mapper.toResponse
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component

@Component("userCurrentLoadDelegate")
class UserCurrentLoadDelegate(
    private val processUserResolver: ProcessUserResolver,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val actor = processUserResolver.resolveActor(execution)
        execution.setVariable("resultSummary", CamundaPresentation.formatUser(actor.toResponse()))
    }
}
