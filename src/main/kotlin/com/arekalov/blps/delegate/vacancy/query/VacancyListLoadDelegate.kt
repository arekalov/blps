package com.arekalov.blps.delegate.vacancy.query

import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.camunda.ProcessUserResolver
import com.arekalov.blps.model.enum.VacancyStatus
import com.arekalov.blps.service.VacancyService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component("vacancyListLoadDelegate")
class VacancyListLoadDelegate(
    private val vacancyService: VacancyService,
    private val processUserResolver: ProcessUserResolver,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val page = CamundaPresentation.intVar(execution, "page", 0)
        val size = CamundaPresentation.intVar(execution, "size", 20)
        val pageable = PageRequest.of(page, size)

        val status = (execution.getVariable("status") as? String)
            ?.takeIf { it.isNotBlank() }
            ?.let { VacancyStatus.valueOf(it) }

        val my = CamundaPresentation.boolVar(execution, "my")
        val result = if (my) {
            val actor = try {
                processUserResolver.resolveActor(execution)
            } catch (e: Exception) {
                execution.setVariable(
                    "resultSummary",
                    "Для фильтра «Только мои вакансии» войдите в Tasklist под своим email.",
                )
                execution.setVariable("finish", false)
                return
            }
            vacancyService.getMyVacancies(actor.id!!, status, pageable)
        } else {
            vacancyService.getAllVacancies(status, pageable)
        }

        execution.setVariable("resultSummary", CamundaPresentation.formatVacancyList(result))
        execution.setVariable("finish", false)
    }
}
