package com.arekalov.blps.delegate.tariff.query

import com.arekalov.blps.repository.TariffRepository
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component

@Component("tariffListDelegate")
class TariffListDelegate(
    private val tariffRepository: TariffRepository,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val tariffs = tariffRepository.findAll()

        // Build list of label/value pairs for the form select field
        val tariffOptions = tariffs.map { tariff ->
            mapOf(
                "value" to tariff.id.toString(),
                "label" to "${tariff.name} — ${tariff.price} руб. / ${tariff.durationDays} дн.",
            )
        }

        execution.setVariable("tariffOptions", tariffOptions)
    }
}
