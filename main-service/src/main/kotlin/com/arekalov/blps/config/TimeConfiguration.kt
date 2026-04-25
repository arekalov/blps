package com.arekalov.blps.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class TimeConfiguration {

    /**
     * Должен совпадать с зоной, в которой в приложении задаётся «локальное» время без смещения
     * ([java.time.LocalDateTime.now] в сервисах), иначе native SQL сравнит `published_at` с `asOf` в разных смыслах.
     */
    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()
}
