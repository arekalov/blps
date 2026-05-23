package com.arekalov.blps.camunda

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin
import org.camunda.bpm.engine.impl.plugin.AdministratorAuthorizationPlugin
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CamundaConfig(
    private val blpsIdentityProvider: BlpsIdentityProvider,
) {

    @Bean
    fun blpsIdentityProviderPlugin(): ProcessEnginePlugin {
        return object : AbstractCamundaConfiguration() {
            override fun preInit(configuration: SpringProcessEngineConfiguration) {
                configuration.identityProviderSessionFactory = BlpsIdentityProviderFactory(blpsIdentityProvider)
            }
        }
    }

    @Bean
    fun administratorAuthorizationPlugin(): ProcessEnginePlugin {
        val plugin = AdministratorAuthorizationPlugin()
        plugin.administratorGroupName = BlpsCamundaAuthorizationConfig.GROUP_ADMIN
        return plugin
    }
}
