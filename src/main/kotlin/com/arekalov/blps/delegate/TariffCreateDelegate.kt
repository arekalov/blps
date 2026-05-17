package com.arekalov.blps.delegate

import com.arekalov.blps.dto.tariff.CreateTariffRequest
import com.arekalov.blps.service.TariffService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component("tariffCreateDelegate")
class TariffCreateDelegate(
    private val tariffService: TariffService,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val request = CreateTariffRequest(
            name = execution.getVariable("tariffName") as String,
            price = BigDecimal(execution.getVariable("tariffPrice").toString()),
            durationDays = (execution.getVariable("tariffDurationDays") as Number).toInt(),
            description = execution.getVariable("tariffDescription") as String,
        )

        val created = tariffService.createTariff(request)
        execution.setVariable("createdTariffId", created.id.toString())
        execution.setVariable("createdTariffName", created.name)
    }
}
