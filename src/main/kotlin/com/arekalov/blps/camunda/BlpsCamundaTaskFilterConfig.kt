package com.arekalov.blps.camunda

import org.camunda.bpm.engine.AuthorizationService
import org.camunda.bpm.engine.FilterService
import org.camunda.bpm.engine.authorization.Authorization
import org.camunda.bpm.engine.authorization.Permissions
import org.camunda.bpm.engine.authorization.Resources
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
class BlpsCamundaTaskFilterConfig(
    private val filterService: FilterService,
    private val authorizationService: AuthorizationService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    @Order(300)
    fun setupFilters() {
        listOf(FILTER_MY_TASKS, FILTER_ALL_TASKS, FILTER_MODERATION).forEach { name ->
            filterService.createFilterQuery().filterName(name).list()
                .forEach { filterService.deleteFilter(it.id) }
        }

        val myTasksFilterId = saveTaskFilter(FILTER_MY_TASKS, authorTasksFilterProperties())

        listOf(
            BlpsCamundaAuthorizationConfig.GROUP_EMPLOYER,
            BlpsCamundaAuthorizationConfig.GROUP_MODERATOR,
            BlpsCamundaAuthorizationConfig.GROUP_ADMIN,
        ).forEach { group ->
            grantFilterRead(group, myTasksFilterId)
        }

        log.info("Tasklist filter configured: {} (initiatorEmail = current user)", FILTER_MY_TASKS)
    }

    private fun authorTasksFilterProperties(): Map<String, Any> =
        mapOf(
            "processVariables" to listOf(
                mapOf(
                    "name" to TaskAuthorAuthorizationListener.VAR_INITIATOR_EMAIL,
                    "operator" to "eq",
                    "value" to "\${currentUser()}",
                ),
            ),
        )

    private fun saveTaskFilter(name: String, queryProperties: Map<String, Any>): String {
        val filter = filterService.newTaskFilter(name)
        filter.properties = queryProperties
        filterService.saveFilter(filter)
        return filter.id
    }

    private fun grantFilterRead(groupId: String, filterId: String) {
        val existing = authorizationService.createAuthorizationQuery()
            .authorizationType(Authorization.AUTH_TYPE_GRANT)
            .groupIdIn(groupId)
            .resourceType(Resources.FILTER)
            .resourceId(filterId)
            .singleResult()

        val auth = existing ?: authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT).also {
            it.groupId = groupId
            it.setResource(Resources.FILTER)
            it.resourceId = filterId
        }
        auth.addPermission(Permissions.READ)
        authorizationService.saveAuthorization(auth)
    }

    companion object {
        const val FILTER_MY_TASKS = "Мои задачи"
        private const val FILTER_ALL_TASKS = "All Tasks"
        private const val FILTER_MODERATION = "Очередь модерации"
    }
}
