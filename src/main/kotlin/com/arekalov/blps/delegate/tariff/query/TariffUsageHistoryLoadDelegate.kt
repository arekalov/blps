package com.arekalov.blps.delegate.tariff.query

import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.service.TariffStatisticsService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.util.UUID

@Component("tariffUsageHistoryLoadDelegate")
class TariffUsageHistoryLoadDelegate(
    private val tariffStatisticsService: TariffStatisticsService,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val tariffId = UUID.fromString(execution.getVariable("tariffId") as String)
        val page = CamundaPresentation.intVar(execution, "page", 0)
        val size = CamundaPresentation.intVar(execution, "size", 20)
        val result = tariffStatisticsService.getTariffUsageHistory(tariffId, PageRequest.of(page, size))
        execution.setVariable("resultSummary", CamundaPresentation.formatUsageHistory(result))
    }
}
