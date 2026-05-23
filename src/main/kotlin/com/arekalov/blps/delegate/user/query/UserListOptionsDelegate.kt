package com.arekalov.blps.delegate.user.query

import com.arekalov.blps.camunda.CamundaFormVariables
import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.repository.UserRepository
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component

@Component("userListOptionsDelegate")
class UserListOptionsDelegate(
    private val userRepository: UserRepository,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val options = userRepository.findAll().map { user ->
            user.id.toString() to "${user.email} (${user.role.name})"
        }

        if (options.isEmpty()) {
            throw IllegalStateException("В системе нет пользователей")
        }

        CamundaFormVariables.setSelectOptions(
            execution,
            "userOptions",
            CamundaPresentation.toSelectOptions(options),
        )
    }
}
