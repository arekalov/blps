package com.arekalov.blps.delegate.moderation.command

import com.arekalov.blps.camunda.ProcessUserResolver
import com.arekalov.blps.service.ModerationService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("moderationApproveDelegate")
class ModerationApproveDelegate(
    private val moderationService: ModerationService,
    private val processUserResolver: ProcessUserResolver,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val vacancyId = UUID.fromString(execution.getVariable("vacancyId") as String)
        val moderatorId = processUserResolver.resolveActor(execution).id!!

        val result = moderationService.approveVacancy(
            moderatorId = moderatorId,
            vacancyId = vacancyId,
        )

        execution.setVariable("vacancyStatus", result.status.name)
        execution.setVariable("publishedAt", result.publishedAt?.toString())
    }
}
