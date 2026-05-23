package com.arekalov.blps.camunda

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.variable.Variables

object CamundaFormVariables {

    private const val JSON_FORMAT = "application/json"
    private val mapper = jacksonObjectMapper()

    /**
     * JSON-массив [{value, label}, ...] для Camunda Forms (valuesExpression).
     * serializedObjectValue + objectTypeName — иначе ObjectValue без типа не пишется в БД.
     */
    fun setSelectOptions(execution: DelegateExecution, variableName: String, options: List<Map<String, String>>) {
        val json = mapper.writeValueAsString(options)
        execution.setVariable(
            variableName,
            Variables.serializedObjectValue(json)
                .serializationDataFormat(JSON_FORMAT)
                .objectTypeName("java.util.ArrayList")
                .create(),
        )
    }
}
