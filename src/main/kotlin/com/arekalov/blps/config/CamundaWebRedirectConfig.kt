package com.arekalov.blps.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CamundaWebRedirectConfig : WebMvcConfigurer {

    override fun addViewControllers(registry: ViewControllerRegistry) {
        // Landing с формой регистрации; Camunda Welcome — для PUBLIC-процессов
        registry.addViewController("/").setViewName("forward:/welcome.html")
        registry.addRedirectViewController("/camunda", "/camunda/app/welcome/default/")
    }
}
