package com.arekalov.blps.delegate.tariff.query

import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.service.TariffService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component("tariffListLoadDelegate")
class TariffListLoadDelegate(
    private val tariffService: TariffService,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val page = CamundaPresentation.intVar(execution, "page", 0)
        val size = CamundaPresentation.intVar(execution, "size", 20)
        val result = tariffService.getAllTariffs(PageRequest.of(page, size))
        execution.setVariable("resultSummary", CamundaPresentation.formatTariffList(result))
    }
}
