package com.arekalov.blps.camunda

import org.camunda.bpm.engine.AuthorizationService
import org.camunda.bpm.engine.IdentityService
import org.camunda.bpm.engine.authorization.Authorization
import org.camunda.bpm.engine.authorization.Permissions
import org.camunda.bpm.engine.authorization.Resources
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.TaskListener
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Выдаёт READ / TASK_WORK только автору процесса ([InitiatorEnrichmentListener] → initiatorEmail).
 * Без TASK * у группы пользователь не увидит чужие задачи в Tasklist.
 */
@Component("taskAuthorAuthorizationListener")
class TaskAuthorAuthorizationListener(
    @Lazy private val authorizationService: AuthorizationService,
    @Lazy private val identityService: IdentityService,
) : TaskListener {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun notify(delegateTask: DelegateTask) {
        val authorEmail = resolveInitiatorEmail(delegateTask) ?: return

        val existing = authorizationService.createAuthorizationQuery()
            .authorizationType(Authorization.AUTH_TYPE_GRANT)
            .userIdIn(authorEmail)
            .resourceType(Resources.TASK)
            .resourceId(delegateTask.id)
            .singleResult()

        val auth = existing ?: authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT).also {
            it.userId = authorEmail
            it.setResource(Resources.TASK)
            it.resourceId = delegateTask.id
        }

        listOf(Permissions.READ, Permissions.UPDATE, Permissions.TASK_WORK).forEach { auth.addPermission(it) }
        authorizationService.saveAuthorization(auth)

        log.debug("Task {} authorized for author {}", delegateTask.id, authorEmail)
    }

    private fun resolveInitiatorEmail(delegateTask: DelegateTask): String? =
        sequenceOf(
            delegateTask.getVariable(VAR_INITIATOR_EMAIL) as? String,
            delegateTask.execution?.getVariable(VAR_INITIATOR_EMAIL) as? String,
            delegateTask.execution?.processInstance?.getVariable(VAR_INITIATOR_EMAIL) as? String,
            delegateTask.getVariable("initiator") as? String,
            delegateTask.execution?.getVariable("initiator") as? String,
            delegateTask.execution?.processInstance?.getVariable("initiator") as? String,
            identityService.currentAuthentication?.userId,
        ).firstOrNull { !it.isNullOrBlank() }

    companion object {
        const val VAR_INITIATOR_EMAIL = "initiatorEmail"
    }
}
