package com.arekalov.blps.camunda

import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.TaskListener
import org.springframework.stereotype.Component

/**
 * Назначает user task на пользователя, запустившего процесс (initiatorEmail),
 * чтобы в Tasklist форма была редактируемой без ручного Claim.
 */
@Component("autoAssignTaskListener")
class AutoAssignTaskListener : TaskListener {

    override fun notify(delegateTask: DelegateTask) {
        if (!delegateTask.assignee.isNullOrBlank()) return

        val initiatorEmail = sequenceOf(
            delegateTask.getVariable("initiatorEmail") as? String,
            delegateTask.execution?.getVariable("initiatorEmail") as? String,
        ).firstOrNull { !it.isNullOrBlank() } ?: return

        delegateTask.assignee = initiatorEmail
    }
}
