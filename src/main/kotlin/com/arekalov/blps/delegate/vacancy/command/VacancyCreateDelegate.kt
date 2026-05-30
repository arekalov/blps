package com.arekalov.blps.delegate.vacancy.command

import com.arekalov.blps.dto.vacancy.CreateVacancyRequest
import com.arekalov.blps.model.enum.EmploymentFormat
import com.arekalov.blps.model.enum.EmploymentType
import com.arekalov.blps.model.enum.ExperienceLevel
import com.arekalov.blps.model.enum.WorkFormat
import com.arekalov.blps.model.enum.WorkSchedule
import com.arekalov.blps.camunda.CamundaDelegateErrors
import com.arekalov.blps.camunda.CamundaDtoValidation
import com.arekalov.blps.camunda.ProcessUserResolver
import com.arekalov.blps.model.enum.UserRole
import com.arekalov.blps.service.VacancyService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component("vacancyCreateDelegate")
class VacancyCreateDelegate(
    private val vacancyService: VacancyService,
    private val processUserResolver: ProcessUserResolver,
    private val camundaDtoValidation: CamundaDtoValidation,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        CamundaDelegateErrors.runWithProcessErrorHandling(execution) {
            val confirmed = execution.getVariable("confirmed")
            if (confirmed != true && confirmed?.toString() != "true") {
                throw IllegalStateException("Подтвердите создание вакансии в форме")
            }

            val initiatorId = processUserResolver.resolveActor(execution).id!!

            val skillsRaw = execution.getVariable("additionalSkills") as? String ?: ""
            val skills = if (skillsRaw.isBlank()) {
                emptyList()
            } else {
                skillsRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }
            }

            val salaryFromRaw = execution.getVariable("salaryFrom")
            val salaryToRaw = execution.getVariable("salaryTo")

            val request = CreateVacancyRequest(
                title = execution.getVariable("title") as String,
                description = execution.getVariable("description") as String,
                experienceLevel = ExperienceLevel.valueOf(execution.getVariable("experienceLevel") as String),
                employmentType = EmploymentType.valueOf(execution.getVariable("employmentType") as String),
                workFormat = WorkFormat.valueOf(execution.getVariable("workFormat") as String),
                employmentFormat = EmploymentFormat.valueOf(execution.getVariable("employmentFormat") as String),
                workSchedule = WorkSchedule.valueOf(execution.getVariable("workSchedule") as String),
                city = execution.getVariable("city") as String,
                salaryFrom = salaryFromRaw?.let { BigDecimal(it.toString()) },
                salaryTo = salaryToRaw?.let { BigDecimal(it.toString()) },
                address = execution.getVariable("address") as? String,
                companyDescription = execution.getVariable("companyDescription") as? String,
                additionalSkills = skills,
            )

            camundaDtoValidation.requireValid(request)
            val vacancy = vacancyService.createVacancy(initiatorId, request)

            val selectedTariffIdStr = execution.getVariable("selectedTariffId") as? String
            if (!selectedTariffIdStr.isNullOrBlank()) {
                val tariffId = UUID.fromString(selectedTariffIdStr)
                vacancyService.selectTariff(
                    userId = initiatorId,
                    vacancyId = UUID.fromString(vacancy.id),
                    tariffId = tariffId,
                    userRole = UserRole.EMPLOYER,
                )
            }

            execution.setVariable("vacancyId", vacancy.id)
            execution.setVariable("vacancyTitle", vacancy.title)
        }
    }
}
