package com.arekalov.blps.config

import com.arekalov.blps.model.Tariff
import com.arekalov.blps.repository.TariffRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import java.math.BigDecimal

@Configuration
class BootstrapTariffsConfig {

    private val log = LoggerFactory.getLogger(BootstrapTariffsConfig::class.java)

    @Bean
    @Order(Int.MIN_VALUE + 1)
    fun bootstrapTariffsRunner(tariffRepository: TariffRepository): ApplicationRunner {
        return ApplicationRunner {
            if (tariffRepository.count() > 0) {
                log.debug("Bootstrap tariffs skipped: tariffs already exist")
                return@ApplicationRunner
            }
            val samples = listOf(
                Tariff(
                    name = "Базовый",
                    price = BigDecimal("999"),
                    durationDays = 7,
                    description = "Размещение вакансии на 7 дней",
                ),
                Tariff(
                    name = "Стандарт",
                    price = BigDecimal("2499"),
                    durationDays = 14,
                    description = "Размещение вакансии на 14 дней",
                ),
                Tariff(
                    name = "Премиум",
                    price = BigDecimal("4999"),
                    durationDays = 30,
                    description = "Размещение вакансии на 30 дней",
                ),
            )
            tariffRepository.saveAll(samples)
            log.info("Bootstrap tariffs created: {}", samples.map { it.name })
        }
    }
}
