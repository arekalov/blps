package com.arekalov.blps.delegate.moderation.command

import com.arekalov.blps.camunda.ProcessUserResolver
import com.arekalov.blps.service.ModerationService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("moderationRejectDelegate")
class ModerationRejectDelegate(
    private val moderationService: ModerationService,
    private val processUserResolver: ProcessUserResolver,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val vacancyId = UUID.fromString(execution.getVariable("vacancyId") as String)
        val rejectionReason = execution.getVariable("rejectionReason") as? String
            ?: throw IllegalStateException("rejectionReason is required for rejection")
        val moderatorId = processUserResolver.resolveActor(execution).id!!

        val result = moderationService.rejectVacancy(
            moderatorId = moderatorId,
            vacancyId = vacancyId,
            reason = rejectionReason,
        )

        execution.setVariable("vacancyStatus", result.status.name)
        execution.setVariable("rejectionReason", result.rejectionReason)
    }
}
