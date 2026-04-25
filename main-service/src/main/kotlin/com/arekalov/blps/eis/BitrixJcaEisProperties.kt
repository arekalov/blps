package com.arekalov.blps.eis

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("blps.eis.bitrix-jca")
data class BitrixJcaEisProperties(
    val enabled: Boolean = true,
    val gatewayBaseUrl: String? = null,
    val crmDealAddPath: String = "/eis/crm.deal.add",
    val vacancyApiPublicBaseUrl: String? = "http://localhost:8080",
    val vacancyApiPathPrefix: String = "/blps/api/v1/vacancies",
    val dealCategoryId: Int? = null,
    val initialStageId: String? = null,
    val vacancyIdFieldCode: String? = null,
    val vacancyUrlFieldCode: String? = null,
    val employerFieldCode: String? = null,
    val tariffNameFieldCode: String? = null,
    val tariffDurationDaysFieldCode: String? = null,
    val candidateFoundStageId: String = "C1:WON",
    val inboundSecret: String? = null,
)
