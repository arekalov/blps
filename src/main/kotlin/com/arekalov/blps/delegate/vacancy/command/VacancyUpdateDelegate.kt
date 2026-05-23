package com.arekalov.blps.delegate.vacancy.command

import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.camunda.ProcessUserResolver
import com.arekalov.blps.dto.vacancy.UpdateVacancyRequest
import com.arekalov.blps.model.enum.EmploymentFormat
import com.arekalov.blps.model.enum.EmploymentType
import com.arekalov.blps.model.enum.ExperienceLevel
import com.arekalov.blps.model.enum.WorkFormat
import com.arekalov.blps.model.enum.WorkSchedule
import com.arekalov.blps.service.VacancyService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component("vacancyUpdateDelegate")
class VacancyUpdateDelegate(
    private val vacancyService: VacancyService,
    private val processUserResolver: ProcessUserResolver,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val actor = processUserResolver.resolveActor(execution)
        val role = processUserResolver.resolveActorRole(execution, actor)
        val vacancyId = UUID.fromString(execution.getVariable("vacancyId") as String)

        val skillsRaw = execution.getVariable("additionalSkills") as? String
        val skills = skillsRaw?.takeIf { it.isNotBlank() }
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }

        val request = UpdateVacancyRequest(
            title = execution.getVariable("title") as? String,
            description = execution.getVariable("description") as? String,
            experienceLevel = (execution.getVariable("experienceLevel") as? String)
                ?.let { ExperienceLevel.valueOf(it) },
            employmentType = (execution.getVariable("employmentType") as? String)
                ?.let { EmploymentType.valueOf(it) },
            workFormat = (execution.getVariable("workFormat") as? String)?.let { WorkFormat.valueOf(it) },
            employmentFormat = (execution.getVariable("employmentFormat") as? String)
                ?.let { EmploymentFormat.valueOf(it) },
            workSchedule = (execution.getVariable("workSchedule") as? String)?.let { WorkSchedule.valueOf(it) },
            city = execution.getVariable("city") as? String,
            salaryFrom = (execution.getVariable("salaryFrom") as? String)
                ?.takeIf { it.isNotBlank() }
                ?.let { BigDecimal(it) },
            salaryTo = (execution.getVariable("salaryTo") as? String)
                ?.takeIf { it.isNotBlank() }
                ?.let { BigDecimal(it) },
            address = execution.getVariable("address") as? String,
            companyDescription = execution.getVariable("companyDescription") as? String,
            additionalSkills = skills,
        )

        val result = vacancyService.updateVacancy(actor.id!!, vacancyId, role, request)
        execution.setVariable("resultSummary", "Вакансия обновлена.\n\n" + CamundaPresentation.formatVacancy(result))
        execution.setVariable("showResult", true)
    }
}
