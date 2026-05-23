package com.arekalov.blps.delegate.auth

import com.arekalov.blps.dto.auth.RegisterRequest
import com.arekalov.blps.service.AuthService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component

@Component("registerDelegate")
class RegisterDelegate(
    private val authService: AuthService,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val email = execution.getVariable("email") as String
        val password = execution.getVariable("password") as String
        val companyName = execution.getVariable("companyName") as String

        val request = RegisterRequest(
            email = email,
            password = password,
            companyName = companyName,
        )

        val userResponse = authService.register(request)
        execution.setVariable("registeredUserId", userResponse.id.toString())
        execution.setVariable("registeredUserEmail", userResponse.email)
        execution.setVariable(
            "resultSummary",
            """
            |Регистрация успешна.
            |Email: ${userResponse.email}
            |Роль: EMPLOYER
            |
            |Войдите в Tasklist (Camunda → Tasklist → Login) тем же email и паролем.
            """.trimMargin(),
        )
        execution.setVariable("showResult", true)
    }
}
