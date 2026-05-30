package com.arekalov.blps.camunda

import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.TaskListener
import org.springframework.stereotype.Component

@Component("processErrorTaskListener")
class ProcessErrorTaskListener : TaskListener {

    override fun notify(delegateTask: DelegateTask) {
        if (delegateTask.taskDefinitionKey != TASK_FILL_VACANCY) return

        if (isProcessError(delegateTask)) {
            delegateTask.name = TASK_NAME_ERROR
        } else {
            delegateTask.name = TASK_NAME_DEFAULT
        }
    }

    private fun isProcessError(delegateTask: DelegateTask): Boolean {
        val flag = delegateTask.getVariable(VAR_PROCESS_ERROR)
            ?: delegateTask.execution?.getVariable(VAR_PROCESS_ERROR)
            ?: delegateTask.execution?.processInstance?.getVariable(VAR_PROCESS_ERROR)
        return flag == true || flag?.toString().equals("true", ignoreCase = true)
    }

    companion object {
        const val TASK_FILL_VACANCY = "task-fill-vacancy"
        const val TASK_NAME_DEFAULT = "Создание вакансии"
        const val TASK_NAME_ERROR = "Создание вакансии (ошибка заполнения)"
        const val VAR_PROCESS_ERROR = "processError"
    }
}
