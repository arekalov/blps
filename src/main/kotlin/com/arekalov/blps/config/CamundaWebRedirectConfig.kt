package com.arekalov.blps.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CamundaWebRedirectConfig : WebMvcConfigurer {

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addRedirectViewController("/", "/camunda/app/tasklist/default/")
        registry.addRedirectViewController("/camunda", "/camunda/app/tasklist/default/")
    }
}
