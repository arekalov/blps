package com.arekalov.blps.delegate.moderation.query

import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.service.ModerationService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component("moderationPendingListDelegate")
class ModerationPendingListDelegate(
    private val moderationService: ModerationService,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val page = CamundaPresentation.intVar(execution, "page", 0)
        val size = CamundaPresentation.intVar(execution, "size", 20)
        val result = moderationService.getPendingVacancies(PageRequest.of(page, size))
        val count = moderationService.getPendingVacanciesCount()
        execution.setVariable(
            "resultSummary",
            "Ожидают модерации: $count\n\n" + CamundaPresentation.formatVacancyList(result),
        )
    }
}
