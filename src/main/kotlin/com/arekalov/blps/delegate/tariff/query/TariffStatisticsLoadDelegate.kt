package com.arekalov.blps.delegate.tariff.query

import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.service.TariffStatisticsService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.util.UUID

@Component("tariffStatisticsLoadDelegate")
class TariffStatisticsLoadDelegate(
    private val tariffStatisticsService: TariffStatisticsService,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val tariffId = UUID.fromString(execution.getVariable("tariffId") as String)
        val stats = tariffStatisticsService.getTariffStatistics(tariffId)
        execution.setVariable("resultSummary", CamundaPresentation.formatTariffStatistics(stats))
    }
}
