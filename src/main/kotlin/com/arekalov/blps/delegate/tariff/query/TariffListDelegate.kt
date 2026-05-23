package com.arekalov.blps.delegate.tariff.query

import com.arekalov.blps.camunda.CamundaFormVariables
import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.repository.TariffRepository
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component

@Component("tariffListDelegate")
class TariffListDelegate(
    private val tariffRepository: TariffRepository,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val options = tariffRepository.findAll().map { tariff ->
            tariff.id.toString() to "${tariff.name} — ${tariff.price} руб. / ${tariff.durationDays} дн."
        }

        if (options.isEmpty()) {
            throw IllegalStateException(
                "В системе нет тарифов. Создайте тариф (процесс «Управление тарифами») под ADMIN.",
            )
        }

        CamundaFormVariables.setSelectOptions(
            execution,
            "tariffOptions",
            CamundaPresentation.toSelectOptions(options),
        )
    }
}
