package com.arekalov.blps.delegate.vacancy.command

import com.arekalov.blps.camunda.CamundaDelegateErrors
import com.arekalov.blps.camunda.ProcessUserResolver
import com.arekalov.blps.model.enum.UserRole
import com.arekalov.blps.service.VacancyService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("vacancyPublishDelegate")
class VacancyPublishDelegate(
    private val vacancyService: VacancyService,
    private val processUserResolver: ProcessUserResolver,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        CamundaDelegateErrors.runWithProcessErrorHandling(execution) {
            val submit = execution.getVariable("submitForModeration")
            if (submit != true && submit?.toString() != "true") {
                throw IllegalStateException("Подтвердите отправку на модерацию в форме")
            }

            val vacancyIdRaw = execution.getVariable("vacancyId") as? String
            if (vacancyIdRaw.isNullOrBlank()) {
                throw IllegalStateException("Вакансия не создана. Исправьте данные и отправьте форму снова.")
            }
            val vacancyId = UUID.fromString(vacancyIdRaw)
            val actor = processUserResolver.resolveActor(execution)
            val role = processUserResolver.resolveActorRole(execution, actor)

            val result = vacancyService.publishVacancy(
                userId = actor.id!!,
                vacancyId = vacancyId,
                userRole = role,
            )

            execution.setVariable("vacancyStatus", result.status.name)
            execution.setVariable(
                "resultSummary",
                "Вакансия отправлена на модерацию.\nСтатус: ${result.status}\nID: ${result.id}",
            )
            execution.setVariable("showResult", true)
        }
    }
}
