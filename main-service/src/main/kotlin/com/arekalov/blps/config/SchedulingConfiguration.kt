package com.arekalov.blps.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = ["blps.scheduling.enabled"], havingValue = "true", matchIfMissing = true)
class SchedulingConfiguration
