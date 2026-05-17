package com.arekalov.blps.delegate.moderation.command

import com.arekalov.blps.camunda.ProcessUserResolver
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component

@Component("moderationReviewPrepareDelegate")
class ModerationReviewPrepareDelegate(
    private val processUserResolver: ProcessUserResolver,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val actor = processUserResolver.resolveActor(execution)
        execution.setVariable("moderatorUserId", actor.id.toString())
        execution.setVariable("moderatorEmail", actor.email)
    }
}
