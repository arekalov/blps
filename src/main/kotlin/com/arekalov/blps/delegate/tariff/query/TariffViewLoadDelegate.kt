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
        val tariffId = UUID.fromString(execution.getVariable("tariffId") as String)
        val tariff = tariffService.getTariffById(tariffId)
        execution.setVariable("resultSummary", CamundaPresentation.formatTariff(tariff))
    }
}
