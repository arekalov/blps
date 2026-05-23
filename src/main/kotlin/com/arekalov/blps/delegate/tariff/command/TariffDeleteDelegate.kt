package com.arekalov.blps.delegate.tariff.command

import com.arekalov.blps.service.TariffService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("tariffDeleteDelegate")
class TariffDeleteDelegate(
    private val tariffService: TariffService,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val confirmed = execution.getVariable("deleteConfirmed")
        if (confirmed != true && confirmed?.toString() != "true") {
            throw IllegalStateException("Подтвердите удаление тарифа в форме")
        }

        val tariffId = UUID.fromString(execution.getVariable("tariffId") as String)
        tariffService.deleteTariff(tariffId)
        execution.setVariable("deletedTariffId", tariffId.toString())
        execution.setVariable("resultSummary", "Тариф $tariffId удалён.")
        execution.setVariable("showResult", true)
    }
}
