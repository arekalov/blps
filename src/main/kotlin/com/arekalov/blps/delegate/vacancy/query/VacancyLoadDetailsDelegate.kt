package com.arekalov.blps.delegate.vacancy.query

import com.arekalov.blps.service.VacancyService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("vacancyLoadDetailsDelegate")
class VacancyLoadDetailsDelegate(
    private val vacancyService: VacancyService,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val vacancyId = UUID.fromString(execution.getVariable("vacancyId") as String)
        val vacancy = vacancyService.getVacancyById(vacancyId)
        execution.setVariable("vacancyId", vacancy.id)
        execution.setVariable("title", vacancy.title)
        execution.setVariable("city", vacancy.city)
    }
}
