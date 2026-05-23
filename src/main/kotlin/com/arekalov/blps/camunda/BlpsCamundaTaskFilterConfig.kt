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

/**
 * Tasklist filters + READ authorizations (без READ на FILTER Tasklist показывает «No filter available»).
 */
@Component
class BlpsCamundaTaskFilterConfig(
    private val filterService: FilterService,
    private val authorizationService: AuthorizationService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    @Order(300)
    fun setupFilters() {
        listOf(FILTER_ALL_TASKS, FILTER_MY_TASKS, FILTER_MODERATION).forEach { name ->
            filterService.createFilterQuery().filterName(name).list()
                .forEach { filterService.deleteFilter(it.id) }
        }

        val allTasksFilterId = saveTaskFilter(FILTER_ALL_TASKS, emptyMap())
        val myTasksFilterId = saveTaskFilter(
            FILTER_MY_TASKS,
            mapOf("involvedUserExpression" to "\${currentUser()}"),
        )
        val moderationFilterId = saveTaskFilter(
            FILTER_MODERATION,
            mapOf("candidateGroupIn" to listOf("MODERATOR", "ADMIN")),
        )

        listOf(allTasksFilterId, myTasksFilterId, moderationFilterId).forEach { filterId ->
            grantFilterRead(BlpsCamundaAuthorizationConfig.GROUP_EMPLOYER, filterId)
            grantFilterRead(BlpsCamundaAuthorizationConfig.GROUP_MODERATOR, filterId)
            grantFilterRead(BlpsCamundaAuthorizationConfig.GROUP_ADMIN, filterId)
        }

        log.info(
            "Tasklist filters configured: {}, {}, {}",
            FILTER_ALL_TASKS,
            FILTER_MY_TASKS,
            FILTER_MODERATION,
        )
    }

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
        const val FILTER_ALL_TASKS = "All Tasks"
        const val FILTER_MY_TASKS = "Мои задачи"
        const val FILTER_MODERATION = "Очередь модерации"
    }
}
