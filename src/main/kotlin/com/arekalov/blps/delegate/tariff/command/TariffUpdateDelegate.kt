package com.arekalov.blps.delegate.tariff.command

import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.dto.tariff.UpdateTariffRequest
import com.arekalov.blps.service.TariffService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component("tariffUpdateDelegate")
class TariffUpdateDelegate(
    private val tariffService: TariffService,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val tariffId = UUID.fromString(execution.getVariable("tariffId") as String)

        val request = UpdateTariffRequest(
            name = execution.getVariable("tariffName") as? String,
            price = (execution.getVariable("tariffPrice") as? Number)?.let { BigDecimal(it.toString()) },
            durationDays = (execution.getVariable("tariffDurationDays") as? Number)?.toInt(),
            description = execution.getVariable("tariffDescription") as? String,
        )

        val updated = tariffService.updateTariff(tariffId, request)
        execution.setVariable("updatedTariffId", updated.id.toString())
        execution.setVariable("resultSummary", "Тариф обновлён.\n\n" + CamundaPresentation.formatTariff(updated))
        execution.setVariable("showResult", true)
    }
}
