package com.arekalov.blps.delegate

import com.arekalov.blps.repository.UserRepository
import com.arekalov.blps.service.ModerationService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("moderationRejectDelegate")
class ModerationRejectDelegate(
    private val moderationService: ModerationService,
    private val userRepository: UserRepository,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val vacancyId = UUID.fromString(execution.getVariable("vacancyId") as String)
        val rejectionReason = execution.getVariable("rejectionReason") as? String
            ?: throw IllegalStateException("rejectionReason is required for rejection")
        val moderatorId = resolveModeratorId(execution)

        val result = moderationService.rejectVacancy(
            moderatorId = moderatorId,
            vacancyId = vacancyId,
            reason = rejectionReason,
        )

        execution.setVariable("vacancyStatus", result.status.name)
        execution.setVariable("rejectionReason", result.rejectionReason)
    }

    private fun resolveModeratorId(execution: DelegateExecution): UUID {
        val moderatorIdStr = execution.getVariable("moderatorUserId") as? String
        if (!moderatorIdStr.isNullOrBlank()) return UUID.fromString(moderatorIdStr)

        val email = execution.getVariable("moderatorEmail") as? String
            ?: throw IllegalStateException("moderatorUserId or moderatorEmail variable required")
        return userRepository.findByEmail(email)?.id
            ?: throw IllegalStateException("Moderator not found by email: $email")
    }
}
