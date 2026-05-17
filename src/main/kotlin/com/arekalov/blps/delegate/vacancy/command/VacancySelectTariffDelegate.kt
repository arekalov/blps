package com.arekalov.blps.delegate.vacancy.command

import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.camunda.ProcessUserResolver
import com.arekalov.blps.service.VacancyService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("vacancySelectTariffDelegate")
class VacancySelectTariffDelegate(
    private val vacancyService: VacancyService,
    private val processUserResolver: ProcessUserResolver,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val actor = processUserResolver.resolveActor(execution)
        val role = processUserResolver.resolveActorRole(execution, actor)
        val vacancyId = UUID.fromString(execution.getVariable("vacancyId") as String)
        val tariffId = UUID.fromString(
            (execution.getVariable("selectedTariffId") ?: execution.getVariable("tariffId")) as String,
        )

        val result = vacancyService.selectTariff(actor.id!!, vacancyId, tariffId, role)
        execution.setVariable("resultSummary", "Тариф назначен.\n\n" + CamundaPresentation.formatVacancy(result))
    }
}
