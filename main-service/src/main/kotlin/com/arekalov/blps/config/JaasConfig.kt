package com.arekalov.blps.config

import com.arekalov.blps.jaas.BlpsJaasConfiguration
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.core.annotation.Order
import javax.security.auth.login.Configuration as JaasConfiguration

@Configuration
@Order(Int.MIN_VALUE + 100)
@DependsOn("blpsJaasBridge")
class JaasConfig {

    @PostConstruct
    fun initJaas() {
        JaasConfiguration.setConfiguration(BlpsJaasConfiguration())
    }
}
