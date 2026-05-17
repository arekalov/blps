package com.arekalov.blps.camunda

import com.arekalov.blps.model.enum.UserRole
import com.arekalov.blps.repository.UserRepository
import org.camunda.bpm.engine.identity.Group
import org.camunda.bpm.engine.impl.GroupQueryImpl
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity

class BlpsGroupQuery(
    private val userRepository: UserRepository,
) : GroupQueryImpl() {

    override fun executeList(commandContext: CommandContext?, page: org.camunda.bpm.engine.impl.Page?): List<Group> {
        val allRoles = UserRole.entries.map { role ->
            val group = GroupEntity()
            group.id = role.name
            group.name = role.name
            group.type = "WORKFLOW"
            group
        }

        return if (userId != null) {
            val user = userRepository.findByEmail(userId) ?: return emptyList()
            allRoles.filter { it.id == user.role.name }
        } else if (id != null) {
            allRoles.filter { it.id == id }
        } else {
            allRoles
        }
    }

    override fun executeCount(commandContext: CommandContext?): Long {
        return executeList(commandContext, null).size.toLong()
    }
}
