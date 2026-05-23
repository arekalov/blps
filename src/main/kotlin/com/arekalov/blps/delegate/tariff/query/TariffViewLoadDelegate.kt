package com.arekalov.blps.delegate.tariff.query

import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.service.TariffService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("tariffViewLoadDelegate")
class TariffViewLoadDelegate(
    private val tariffService: TariffService,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val idRaw = execution.getVariable("tariffId") as? String
        if (idRaw.isNullOrBlank()) {
            execution.setVariable("resultSummary", "Укажите ID тарифа.")
            execution.setVariable("finish", false)
            return
        }

        val tariffId = try {
            UUID.fromString(idRaw.trim())
        } catch (_: IllegalArgumentException) {
            execution.setVariable("resultSummary", "Некорректный UUID: $idRaw")
            execution.setVariable("finish", false)
            return
        }

        val tariff = tariffService.getTariffById(tariffId)
        execution.setVariable("resultSummary", CamundaPresentation.formatTariff(tariff))
        execution.setVariable("finish", false)
    }
}
