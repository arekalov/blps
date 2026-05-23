package com.arekalov.blps.camunda

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("initiatorEnrichmentListener")
class InitiatorEnrichmentListener(
    private val processUserResolver: ProcessUserResolver,
) : ExecutionListener {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun notify(execution: DelegateExecution) {
        val existingEmail = execution.getVariable("initiatorEmail") as? String
        if (!existingEmail.isNullOrBlank()) return
        runCatching { processUserResolver.enrichExecution(execution) }
            .onFailure { log.warn("Initiator not resolved at process start: {}", it.message) }
    }
}
