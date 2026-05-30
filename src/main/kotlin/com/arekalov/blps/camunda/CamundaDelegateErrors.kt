package com.arekalov.blps.camunda

import com.arekalov.blps.exception.ForbiddenException
import com.arekalov.blps.exception.ValidationException
import org.camunda.bpm.engine.delegate.DelegateExecution

object CamundaDelegateErrors {

    fun runWithProcessErrorHandling(execution: DelegateExecution, block: () -> Unit) {
        execution.setVariable("processError", false)
        execution.setVariable("errorMessage", null)
        try {
            block()
        } catch (e: ValidationException) {
            markProcessError(execution, e.message ?: "Ошибка валидации")
        } catch (e: IllegalStateException) {
            markProcessError(execution, e.message ?: "Некорректные данные")
        } catch (e: ForbiddenException) {
            markProcessError(execution, e.message ?: "Недостаточно прав")
        }
    }

    private fun markProcessError(execution: DelegateExecution, message: String) {
        execution.setVariable("processError", true)
        execution.setVariable("errorMessage", message)
        execution.setVariable("showResult", false)
    }
}
