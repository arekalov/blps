package com.arekalov.blps.camunda

import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl
import org.camunda.bpm.engine.impl.task.TaskDefinition
import org.camunda.bpm.engine.impl.util.xml.Element
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
class AutoAssignTaskListenerPluginConfig(
    @Lazy private val autoAssignTaskListener: AutoAssignTaskListener,
    @Lazy private val taskAuthorAuthorizationListener: TaskAuthorAuthorizationListener,
) {

    @Bean
    fun autoAssignTaskListenerPlugin(): ProcessEnginePlugin {
        return object : AbstractCamundaConfiguration() {
            override fun preInit(configuration: SpringProcessEngineConfiguration) {
                val listeners = configuration.customPostBPMNParseListeners?.toMutableList() ?: mutableListOf()
                listeners.add(
                    object : AbstractBpmnParseListener() {
                        override fun parseUserTask(userTask: Element?, scope: ScopeImpl?, activity: ActivityImpl?) {
                            val taskDefinition = activity?.getProperty("taskDefinition") as? TaskDefinition ?: return
                            taskDefinition.addTaskListener(
                                TaskListener.EVENTNAME_CREATE,
                                autoAssignTaskListener,
                            )
                            taskDefinition.addTaskListener(
                                TaskListener.EVENTNAME_CREATE,
                                taskAuthorAuthorizationListener,
                            )
                        }
                    },
                )
                configuration.customPostBPMNParseListeners = listeners
            }
        }
    }
}
