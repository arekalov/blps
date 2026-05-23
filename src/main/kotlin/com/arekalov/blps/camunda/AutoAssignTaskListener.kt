package com.arekalov.blps.camunda

import com.arekalov.blps.repository.UserRepository
import org.camunda.bpm.engine.IdentityService
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.model.bpmn.instance.UserTask
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Назначает user task на пользователя, запустившего процесс,
 * если его роль входит в candidateGroups задачи (или candidateGroups не заданы).
 */
@Component("autoAssignTaskListener")
class AutoAssignTaskListener(
    private val userRepository: UserRepository,
    @Lazy private val identityService: IdentityService,
) : TaskListener {

    override fun notify(delegateTask: DelegateTask) {
        if (!delegateTask.assignee.isNullOrBlank()) return

        val initiatorEmail = sequenceOf(
            delegateTask.getVariable("initiatorEmail") as? String,
            delegateTask.execution?.getVariable("initiatorEmail") as? String,
            delegateTask.execution?.processInstance?.getVariable("initiatorEmail") as? String,
            delegateTask.getVariable("initiator") as? String,
            delegateTask.execution?.getVariable("initiator") as? String,
            delegateTask.execution?.processInstance?.getVariable("initiator") as? String,
            identityService.currentAuthentication?.userId,
        ).firstOrNull { !it.isNullOrBlank() } ?: return

        val candidateGroups = delegateTask.bpmnModelElementInstance
            ?.let { it }
            ?.getAttributeValueNs(CAMUNDA_NS, "candidateGroups")
            ?.split(',')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        if (candidateGroups.isNotEmpty()) {
            val roleName = (delegateTask.getVariable("actorRole") as? String)
                ?: userRepository.findByEmail(initiatorEmail)?.role?.name
                ?: return
            if (roleName !in candidateGroups) return
        }

        delegateTask.assignee = initiatorEmail
    }

    companion object {
        private const val CAMUNDA_NS = "http://camunda.org/schema/1.0/bpmn"
    }
}
