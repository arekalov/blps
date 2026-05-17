package com.arekalov.blps.camunda

import com.arekalov.blps.repository.UserRepository
import org.camunda.bpm.engine.identity.User
import org.camunda.bpm.engine.impl.UserQueryImpl
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity

class BlpsUserQuery(
    private val userRepository: UserRepository,
) : UserQueryImpl() {

    override fun executeList(commandContext: CommandContext?, page: org.camunda.bpm.engine.impl.Page?): List<User> {
        val allUsers = userRepository.findAll()

        return allUsers
            .filter { user ->
                id == null || user.email == id
            }
            .filter { user ->
                groupId == null || user.role.name == groupId
            }
            .map { user ->
                val entity = UserEntity()
                entity.id = user.email
                entity.firstName = user.companyName
                entity.lastName = user.role.name
                entity.email = user.email
                entity
            }
    }

    override fun executeCount(commandContext: CommandContext?): Long {
        return executeList(commandContext, null).size.toLong()
    }
}
