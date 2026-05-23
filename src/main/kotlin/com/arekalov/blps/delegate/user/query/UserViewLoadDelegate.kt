package com.arekalov.blps.delegate.user.query

import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.service.UserService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("userViewLoadDelegate")
class UserViewLoadDelegate(
    private val userService: UserService,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val idRaw = execution.getVariable("targetUserId") as? String
        if (idRaw.isNullOrBlank()) {
            execution.setVariable("resultSummary", "Выберите пользователя.")
            execution.setVariable("finish", false)
            return
        }

        val userId = try {
            UUID.fromString(idRaw.trim())
        } catch (_: IllegalArgumentException) {
            execution.setVariable("resultSummary", "Некорректный UUID: $idRaw")
            execution.setVariable("finish", false)
            return
        }

        val user = userService.getUserById(userId)
        execution.setVariable("resultSummary", CamundaPresentation.formatUser(user))
        execution.setVariable("finish", false)
    }
}
