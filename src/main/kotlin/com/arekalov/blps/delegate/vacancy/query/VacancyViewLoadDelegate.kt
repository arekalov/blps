package com.arekalov.blps.delegate.vacancy.query

import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.service.VacancyService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("vacancyViewLoadDelegate")
class VacancyViewLoadDelegate(
    private val vacancyService: VacancyService,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val vacancyId = UUID.fromString(execution.getVariable("vacancyId") as String)
        val vacancy = vacancyService.getVacancyById(vacancyId)
        execution.setVariable("resultSummary", CamundaPresentation.formatVacancy(vacancy))
    }
}
