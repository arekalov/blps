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

        val page = CamundaPresentation.intVar(execution, "page", 0)
        val size = CamundaPresentation.intVar(execution, "size", 20)
        val result = tariffStatisticsService.getTariffUsageHistory(tariffId, PageRequest.of(page, size))
        execution.setVariable("resultSummary", CamundaPresentation.formatUsageHistory(result))
        execution.setVariable("finish", false)
    }
}
