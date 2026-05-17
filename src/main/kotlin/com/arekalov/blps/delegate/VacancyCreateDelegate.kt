package com.arekalov.blps.delegate

import com.arekalov.blps.dto.vacancy.CreateVacancyRequest
import com.arekalov.blps.model.enum.EmploymentFormat
import com.arekalov.blps.model.enum.EmploymentType
import com.arekalov.blps.model.enum.ExperienceLevel
import com.arekalov.blps.model.enum.WorkFormat
import com.arekalov.blps.model.enum.WorkSchedule
import com.arekalov.blps.repository.TariffRepository
import com.arekalov.blps.repository.UserRepository
import com.arekalov.blps.service.VacancyService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component("vacancyCreateDelegate")
class VacancyCreateDelegate(
    private val vacancyService: VacancyService,
    private val userRepository: UserRepository,
    private val tariffRepository: TariffRepository,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val initiatorId = resolveInitiatorId(execution)

        val skillsRaw = execution.getVariable("additionalSkills") as? String ?: ""
        val skills = if (skillsRaw.isBlank()) emptyList()
        else skillsRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }

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

        val vacancy = vacancyService.createVacancy(initiatorId, request)

        // Assign selected tariff if provided
        val selectedTariffIdStr = execution.getVariable("selectedTariffId") as? String
        if (!selectedTariffIdStr.isNullOrBlank()) {
            val tariffId = UUID.fromString(selectedTariffIdStr)
            vacancyService.selectTariff(
                userId = initiatorId,
                vacancyId = UUID.fromString(vacancy.id),
                tariffId = tariffId,
                userRole = com.arekalov.blps.model.enum.UserRole.EMPLOYER,
            )
        }

        execution.setVariable("vacancyId", vacancy.id)
        execution.setVariable("vacancyTitle", vacancy.title)
    }

    private fun resolveInitiatorId(execution: DelegateExecution): UUID {
        val initiator = execution.getVariable("initiatorUserId") as? String
            ?: execution.getVariable("camunda_userId") as? String
        if (!initiator.isNullOrBlank()) {
            return UUID.fromString(initiator)
        }
        // Fallback: find by email from process variable
        val email = execution.getVariable("initiatorEmail") as? String
            ?: throw IllegalStateException("Cannot resolve initiator: initiatorUserId or initiatorEmail process variable is required")
        return userRepository.findByEmail(email)?.id
            ?: throw IllegalStateException("User with email $email not found")
    }
}
