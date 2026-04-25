package com.arekalov.blps.controller

import com.arekalov.blps.eis.BitrixInboundWebhookService
import com.arekalov.blps.eis.BitrixJcaEisProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.util.StringUtils.hasText
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/integrations/bitrix")
class BitrixIntegrationController(
    private val properties: BitrixJcaEisProperties,
    private val bitrixInboundWebhookService: BitrixInboundWebhookService,
    private val objectMapper: ObjectMapper,
) {

    @PostMapping("/deal-stage-changed")
    fun onDealStageChanged(
        @RequestParam params: MultiValueMap<String, String>,
        @RequestBody(required = false) body: String?,
        @RequestHeader(name = "X-BLPS-BITRIX-SECRET", required = false) headerSecret: String?,
    ): ResponseEntity<Map<String, Any>> {
        if (!isSecretValid(params, headerSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("status" to "forbidden"))
        }
        val stageId = extractStageId(params, body)
        val targetStageId = properties.candidateFoundStageId
        if (stageId == null || stageId != targetStageId) {
            return ResponseEntity.ok(
                mapOf(
                    "status" to "ignored",
                    "reason" to "stage_not_target",
                    "stageId" to (stageId ?: ""),
                    "targetStageId" to targetStageId,
                ),
            )
        }
        val vacancyIdRaw = extractVacancyId(params, body)
            ?: return ResponseEntity.badRequest().body(
                mapOf("status" to "bad_request", "reason" to "vacancy_id_missing"),
            )
        val vacancyId = runCatching { UUID.fromString(vacancyIdRaw) }.getOrNull()
            ?: return ResponseEntity.badRequest().body(
                mapOf("status" to "bad_request", "reason" to "vacancy_id_invalid"),
            )
        val result = bitrixInboundWebhookService.closePublishedVacancyById(vacancyId)
        return ResponseEntity.ok(
            mapOf(
                "status" to "ok",
                "result" to result.name,
                "vacancyId" to vacancyId.toString(),
            ),
        )
    }

    private fun isSecretValid(params: MultiValueMap<String, String>, headerSecret: String?): Boolean {
        val expected = properties.inboundSecret
        if (expected == null || !hasText(expected)) {
            return true
        }
        val q = params.getFirst("secret")
        return expected == headerSecret || expected == q
    }

    private fun extractStageId(params: MultiValueMap<String, String>, body: String?): String? {
        return firstNonBlank(
            params,
            listOf("stageId", "STAGE_ID", "data[FIELDS][STAGE_ID]", "FIELDS[STAGE_ID]"),
        ) ?: jsonField(body, listOf("/stageId", "/STAGE_ID", "/data/FIELDS/STAGE_ID", "/FIELDS/STAGE_ID"))
    }

    private fun extractVacancyId(params: MultiValueMap<String, String>, body: String?): String? {
        val uf = properties.vacancyIdFieldCode?.trim().orEmpty()
        val keys = mutableListOf(
            "vacancyId",
            "VACANCY_ID",
            "data[FIELDS][VACANCY_ID]",
            "FIELDS[VACANCY_ID]",
        )
        if (uf.isNotBlank()) {
            keys += listOf(uf, "data[FIELDS][$uf]", "FIELDS[$uf]")
        }
        val fromParams = firstNonBlank(params, keys)
        if (fromParams != null) {
            return fromParams
        }
        val pointers = mutableListOf(
            "/vacancyId",
            "/VACANCY_ID",
            "/data/FIELDS/VACANCY_ID",
            "/FIELDS/VACANCY_ID",
        )
        if (uf.isNotBlank()) {
            pointers += listOf("/$uf", "/data/FIELDS/$uf", "/FIELDS/$uf")
        }
        return jsonField(body, pointers)
    }

    private fun firstNonBlank(params: MultiValueMap<String, String>, keys: List<String>): String? {
        for (key in keys) {
            val value = params.getFirst(key)
            if (value != null && hasText(value)) {
                return value.trim()
            }
        }
        return null
    }

    private fun jsonField(body: String?, pointers: List<String>): String? {
        if (body == null || !hasText(body)) {
            return null
        }
        val node = runCatching { objectMapper.readTree(body) }.getOrNull() ?: return null
        for (pointer in pointers) {
            val value = node.at(pointer)
            if (!value.isMissingNode && !value.isNull) {
                val asText = value.asText("")
                if (asText.isNotBlank()) {
                    return asText.trim()
                }
            }
        }
        return null
    }
}
