package com.arekalov.blps.delegate.vacancy.command

import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.camunda.ProcessUserResolver
import com.arekalov.blps.service.VacancyService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("vacancyArchiveDelegate")
class VacancyArchiveDelegate(
    private val vacancyService: VacancyService,
    private val processUserResolver: ProcessUserResolver,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val actor = processUserResolver.resolveActor(execution)
        val role = processUserResolver.resolveActorRole(execution, actor)
        val vacancyId = UUID.fromString(execution.getVariable("vacancyId") as String)

        val result = vacancyService.archiveVacancy(actor.id!!, vacancyId, role)
        execution.setVariable("resultSummary", "Вакансия архивирована.\n\n" + CamundaPresentation.formatVacancy(result))
    }
}
