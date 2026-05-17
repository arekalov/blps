package com.arekalov.blps.delegate

import com.arekalov.blps.model.enum.UserRole
import com.arekalov.blps.repository.UserRepository
import com.arekalov.blps.service.VacancyService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("vacancyPublishDelegate")
class VacancyPublishDelegate(
    private val vacancyService: VacancyService,
    private val userRepository: UserRepository,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val vacancyId = UUID.fromString(execution.getVariable("vacancyId") as String)
        val initiatorId = resolveInitiatorId(execution)

        val result = vacancyService.publishVacancy(
            userId = initiatorId,
            vacancyId = vacancyId,
            userRole = UserRole.EMPLOYER,
        )

        execution.setVariable("vacancyStatus", result.status.name)
    }

    private fun resolveInitiatorId(execution: DelegateExecution): UUID {
        val initiator = execution.getVariable("initiatorUserId") as? String
        if (!initiator.isNullOrBlank()) return UUID.fromString(initiator)

        val email = execution.getVariable("initiatorEmail") as? String
            ?: throw IllegalStateException("initiatorUserId or initiatorEmail variable required")
        return userRepository.findByEmail(email)?.id
            ?: throw IllegalStateException("User not found by email: $email")
    }
}
