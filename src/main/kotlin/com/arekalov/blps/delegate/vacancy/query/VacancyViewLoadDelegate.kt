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
        val idRaw = execution.getVariable("vacancyId") as? String
        if (idRaw.isNullOrBlank()) {
            execution.setVariable("resultSummary", "Укажите ID вакансии.")
            execution.setVariable("finish", false)
            return
        }

        val vacancyId = try {
            UUID.fromString(idRaw.trim())
        } catch (_: IllegalArgumentException) {
            execution.setVariable("resultSummary", "Некорректный UUID: $idRaw")
            execution.setVariable("finish", false)
            return
        }

        val vacancy = vacancyService.getVacancyById(vacancyId)
        execution.setVariable("resultSummary", CamundaPresentation.formatVacancy(vacancy))
        execution.setVariable("finish", false)
    }
}
