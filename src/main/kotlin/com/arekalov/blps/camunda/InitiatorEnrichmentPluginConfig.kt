package com.arekalov.blps.camunda

import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl
import org.camunda.bpm.engine.impl.util.xml.Element
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
class InitiatorEnrichmentPluginConfig(
    @Lazy private val initiatorEnrichmentListener: InitiatorEnrichmentListener,
) {

    @Bean
    fun initiatorEnrichmentPlugin(): ProcessEnginePlugin {
        return object : AbstractCamundaConfiguration() {
            override fun preInit(configuration: SpringProcessEngineConfiguration) {
                val listeners = configuration.customPostBPMNParseListeners?.toMutableList() ?: mutableListOf()
                listeners.add(
                    object : AbstractBpmnParseListener() {
                        override fun parseStartEvent(
                            startEventElement: Element?,
                            scope: ScopeImpl?,
                            activity: ActivityImpl?,
                        ) {
                            activity?.addListener(
                                ExecutionListener.EVENTNAME_START,
                                initiatorEnrichmentListener,
                            )
                        }
                    },
                )
                configuration.customPostBPMNParseListeners = listeners
            }
        }
    }
}
