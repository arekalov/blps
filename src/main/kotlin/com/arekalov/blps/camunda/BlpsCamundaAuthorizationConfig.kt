package com.arekalov.blps.camunda

import org.camunda.bpm.engine.AuthorizationService
import org.camunda.bpm.engine.authorization.Authorization
import org.camunda.bpm.engine.authorization.Permissions
import org.camunda.bpm.engine.authorization.Resources
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Camunda authorizations aligned with [docs/ROLES_SPECIFICATION.md].
 */
@Component
@ConditionalOnProperty(name = ["camunda.bpm.authorization.enabled"], havingValue = "true")
class BlpsCamundaAuthorizationConfig(
    private val authorizationService: AuthorizationService,
    @Value("\${camunda.bpm.authorization.enabled:false}")
    private val authorizationEnabled: Boolean,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    @Order(200)
    fun setupAuthorizations() {
        if (!authorizationEnabled) {
            log.warn("Camunda authorization is disabled — skipping BLPS authorization bootstrap")
            return
        }

        setupPublicGuestAccess()

        listOf(GROUP_EMPLOYER, GROUP_MODERATOR, GROUP_ADMIN).forEach { group ->
            ensureGroupGrant(group, Resources.APPLICATION, APP_TASKLIST, Permissions.ACCESS)
            ensureGroupGrant(
                group,
                Resources.TASK,
                ANY_RESOURCE,
                Permissions.READ,
                Permissions.UPDATE,
                Permissions.TASK_WORK,
            )
            ensureGroupGrant(
                group,
                Resources.PROCESS_INSTANCE,
                ANY_RESOURCE,
                Permissions.READ,
                Permissions.CREATE,
            )
        }

        listOf(GROUP_EMPLOYER, GROUP_MODERATOR).forEach { group ->
            PUBLIC_PROCESS_KEYS.forEach { key ->
                ensureProcessStartGrant(group, key)
            }
        }

        EMPLOYER_PROCESS_KEYS.forEach { key ->
            ensureProcessStartGrant(GROUP_EMPLOYER, key)
        }

        MODERATOR_PROCESS_KEYS.forEach { key ->
            ensureProcessStartGrant(GROUP_MODERATOR, key)
        }

        SHARED_AUTHENTICATED_PROCESS_KEYS.forEach { key ->
            ensureProcessStartGrant(GROUP_EMPLOYER, key)
            ensureProcessStartGrant(GROUP_MODERATOR, key)
        }

        ADMIN_ONLY_PROCESS_KEYS.forEach { key ->
            ensureProcessStartGrant(GROUP_ADMIN, key)
        }

        REVOKED_EMPLOYER_PROCESS_KEYS.forEach { key ->
            revokeGroupGrant(GROUP_EMPLOYER, Resources.PROCESS_DEFINITION, key)
        }
        REVOKED_MODERATOR_PROCESS_KEYS.forEach { key ->
            revokeGroupGrant(GROUP_MODERATOR, Resources.PROCESS_DEFINITION, key)
        }

        log.info("BLPS Camunda authorizations updated")
    }

    private fun setupPublicGuestAccess() {
        ensureGlobalGrant(Resources.APPLICATION, APP_WELCOME, Permissions.ACCESS)
        PUBLIC_PROCESS_KEYS.forEach { key ->
            ensureGlobalGrant(Resources.PROCESS_DEFINITION, key, Permissions.READ, Permissions.CREATE_INSTANCE)
        }
        ensureGlobalGrant(Resources.PROCESS_INSTANCE, ANY_RESOURCE, Permissions.CREATE)
    }

    private fun ensureProcessStartGrant(groupId: String, processKey: String) {
        ensureGroupGrant(
            groupId,
            Resources.PROCESS_DEFINITION,
            processKey,
            Permissions.READ,
            Permissions.CREATE_INSTANCE,
        )
    }

    private fun ensureGlobalGrant(resource: Resources, resourceId: String, vararg permissions: Permissions) {
        upsertGrant(Authorization.AUTH_TYPE_GLOBAL, groupId = null, resource, resourceId, *permissions)
    }

    private fun ensureGroupGrant(groupId: String, resource: Resources, resourceId: String, vararg permissions: Permissions) {
        upsertGrant(Authorization.AUTH_TYPE_GRANT, groupId, resource, resourceId, *permissions)
    }

    private fun revokeGroupGrant(groupId: String, resource: Resources, resourceId: String) {
        authorizationService.createAuthorizationQuery()
            .authorizationType(Authorization.AUTH_TYPE_GRANT)
            .groupIdIn(groupId)
            .resourceType(resource)
            .resourceId(resourceId)
            .list()
            .forEach { authorizationService.deleteAuthorization(it.id) }
    }

    private fun upsertGrant(
        type: Int,
        groupId: String?,
        resource: Resources,
        resourceId: String,
        vararg permissions: Permissions,
    ) {
        val query = authorizationService.createAuthorizationQuery()
            .authorizationType(type)
            .resourceType(resource)
            .resourceId(resourceId)
        if (groupId != null) {
            query.groupIdIn(groupId)
        }

        val auth = query.singleResult()
            ?: authorizationService.createNewAuthorization(type).also {
                if (groupId != null) it.groupId = groupId
                it.setResource(resource)
                it.resourceId = resourceId
            }

        permissions.forEach { auth.addPermission(it) }
        authorizationService.saveAuthorization(auth)
    }

    companion object {
        const val GROUP_EMPLOYER = "EMPLOYER"
        const val GROUP_MODERATOR = "MODERATOR"
        const val GROUP_ADMIN = "ADMIN"
        const val APP_TASKLIST = "tasklist"
        const val APP_WELCOME = "welcome"
        const val ANY_RESOURCE = "*"

        val PUBLIC_PROCESS_KEYS = listOf(
            "user-registration",
            "vacancy-list",
            "vacancy-view",
            "tariff-list",
            "tariff-view",
        )

        /** Профиль текущего пользователя — employer/moderator. */
        val SHARED_AUTHENTICATED_PROCESS_KEYS = listOf(
            "user-current",
            "user-management",
        )

        /** Список/просмотр пользователей — только admin. */
        val ADMIN_ONLY_PROCESS_KEYS = listOf(
            "user-list",
            "user-view",
        )

        val EMPLOYER_PROCESS_KEYS = listOf(
            "vacancy-update",
            "vacancy-delete",
            "vacancy-archive",
            "vacancy-publication",
        )

        val MODERATOR_PROCESS_KEYS = listOf(
            "moderation-pending-list",
            "moderation-review",
            "tariff-statistics",
            "tariff-usage-history",
        )

        /** Включены в vacancy-publication — не стартуют из Tasklist. */
        val REVOKED_EMPLOYER_PROCESS_KEYS = listOf(
            "user-list",
            "user-view",
            "vacancy-select-tariff",
            "vacancy-submit-moderation",
        )

        val REVOKED_MODERATOR_PROCESS_KEYS = listOf(
            "user-list",
            "user-view",
        )
    }
}
