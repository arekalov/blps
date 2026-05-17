package com.arekalov.blps.camunda

import org.camunda.bpm.engine.identity.Tenant
import org.camunda.bpm.engine.impl.TenantQueryImpl
import org.camunda.bpm.engine.impl.interceptor.CommandContext

class BlpsTenantQuery : TenantQueryImpl() {

    override fun executeList(commandContext: CommandContext?, page: org.camunda.bpm.engine.impl.Page?): List<Tenant> =
        emptyList()

    override fun executeCount(commandContext: CommandContext?): Long = 0L
}
