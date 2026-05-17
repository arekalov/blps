package com.arekalov.blps.camunda

import org.camunda.bpm.spring.boot.starter.rest.CamundaJerseyResourceConfig
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class BlpsCamundaJerseyResourceConfig : CamundaJerseyResourceConfig()

@Configuration
class BlpsCamundaJerseyConfiguration {

    @Bean
    @ConditionalOnMissingBean(CamundaJerseyResourceConfig::class)
    fun camundaJerseyResourceConfig(): CamundaJerseyResourceConfig = BlpsCamundaJerseyResourceConfig()
}
