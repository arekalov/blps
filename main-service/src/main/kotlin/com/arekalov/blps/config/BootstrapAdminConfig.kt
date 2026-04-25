package com.arekalov.blps.config

import com.arekalov.blps.model.User
import com.arekalov.blps.model.enum.UserRole
import com.arekalov.blps.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class BootstrapAdminConfig(

    @Value("\${blps.bootstrap.admin-email:}")
    private val adminEmail: String,

    @Value("\${blps.bootstrap.admin-password:}")
    private val adminPassword: String,

    @Value("\${blps.bootstrap.admin-company-name:System}")
    private val adminCompanyName: String,
) {

    private val log = LoggerFactory.getLogger(BootstrapAdminConfig::class.java)

    @Bean
    @Order(Int.MIN_VALUE)
    @ConditionalOnProperty(name = ["blps.bootstrap.admin-email"])
    fun bootstrapAdminRunner(
        userRepository: UserRepository,
        passwordEncoder: PasswordEncoder,
    ): ApplicationRunner {
        return ApplicationRunner {
            if (adminEmail.isBlank() || adminPassword.isBlank()) {
                log.debug("Bootstrap admin skipped: admin-email or admin-password not set")
                return@ApplicationRunner
            }
            if (userRepository.existsByRole(UserRole.ADMIN)) {
                log.debug("Bootstrap admin skipped: admin user already exists")
                return@ApplicationRunner
            }
            if (userRepository.existsByEmail(adminEmail)) {
                log.warn("Bootstrap admin skipped: user with email {} already exists (not admin)", adminEmail)
                return@ApplicationRunner
            }
            val admin = User(
                id = null,
                email = adminEmail,
                passwordHash = passwordEncoder.encode(adminPassword),
                companyName = adminCompanyName,
                role = UserRole.ADMIN,
            )
            userRepository.save(admin)
            log.info("Bootstrap admin created: {}", adminEmail)
        }
    }
}
