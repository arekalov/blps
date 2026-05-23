package com.arekalov.blps.delegate.vacancy.command

import com.arekalov.blps.camunda.ProcessUserResolver
import com.arekalov.blps.service.VacancyService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("vacancyDeleteDelegate")
class VacancyDeleteDelegate(
    private val vacancyService: VacancyService,
    private val processUserResolver: ProcessUserResolver,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val confirmed = execution.getVariable("confirmDelete")
        if (confirmed != true && confirmed?.toString() != "true") {
            throw IllegalStateException("Подтвердите удаление вакансии в форме")
        }

        val actor = processUserResolver.resolveActor(execution)
        val role = processUserResolver.resolveActorRole(execution, actor)
        val vacancyId = UUID.fromString(execution.getVariable("vacancyId") as String)

        vacancyService.deleteVacancy(actor.id!!, vacancyId, role)
        execution.setVariable("resultSummary", "Вакансия $vacancyId удалена.")
        execution.setVariable("showResult", true)
    }
}
