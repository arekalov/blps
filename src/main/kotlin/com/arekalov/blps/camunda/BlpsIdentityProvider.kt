package com.arekalov.blps.camunda

import com.arekalov.blps.model.User
import com.arekalov.blps.model.enum.UserRole
import com.arekalov.blps.repository.UserRepository
import org.camunda.bpm.engine.identity.Group
import org.camunda.bpm.engine.identity.GroupQuery
import org.camunda.bpm.engine.identity.NativeUserQuery
import org.camunda.bpm.engine.identity.Tenant
import org.camunda.bpm.engine.identity.TenantQuery
import org.camunda.bpm.engine.identity.UserQuery
import org.camunda.bpm.engine.impl.GroupQueryImpl
import org.camunda.bpm.engine.impl.UserQueryImpl
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class BlpsIdentityProvider(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : ReadOnlyIdentityProvider {

    private val logger = LoggerFactory.getLogger(BlpsIdentityProvider::class.java)

    override fun checkPassword(userId: String, password: String): Boolean {
        val user = userRepository.findByEmail(userId) ?: return false
        return passwordEncoder.matches(password, user.passwordHash)
    }

    override fun findUserById(userId: String): org.camunda.bpm.engine.identity.User? {
        val user = userRepository.findByEmail(userId) ?: return null
        return user.toCamundaUser()
    }

    override fun createUserQuery(): UserQuery = BlpsUserQuery(userRepository)

    override fun createUserQuery(commandContext: CommandContext): UserQuery = BlpsUserQuery(userRepository)

    override fun createNativeUserQuery(): NativeUserQuery {
        throw UnsupportedOperationException("Native queries not supported")
    }

    override fun findGroupById(groupId: String): Group? {
        return if (UserRole.entries.any { it.name == groupId }) {
            val group = GroupEntity()
            group.id = groupId
            group.name = groupId
            group.type = "WORKFLOW"
            group
        } else {
            null
        }
    }

    override fun createGroupQuery(): GroupQuery = BlpsGroupQuery(userRepository)

    override fun createGroupQuery(commandContext: CommandContext): GroupQuery = BlpsGroupQuery(userRepository)

    override fun findTenantById(tenantId: String): Tenant? = null

    override fun createTenantQuery(): TenantQuery = BlpsTenantQuery()

    override fun createTenantQuery(commandContext: CommandContext): TenantQuery = BlpsTenantQuery()

    override fun flush() {}

    override fun close() {}

    private fun User.toCamundaUser(): org.camunda.bpm.engine.identity.User {
        val entity = UserEntity()
        entity.id = email
        entity.firstName = email
        entity.lastName = "$companyName ${role.name}"
        entity.email = email
        return entity
    }
}
