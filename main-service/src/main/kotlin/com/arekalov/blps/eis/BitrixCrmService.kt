package com.arekalov.blps.eis

import com.arekalov.blps.eis.event.VacancyPublishedForBitrixCommitted
import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils.hasText
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@Service
class BitrixCrmService(
    private val properties: BitrixJcaEisProperties,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient: RestClient = RestClient.create()

    fun tryCreateDealForPublishedVacancy(event: VacancyPublishedForBitrixCommitted) {
        if (!properties.enabled) {
            log.debug("Bitrix JCA outbound disabled by config")
            return
        }
        log.info(
            "Bitrix outbound start: vacancyId={} title='{}' employer='{}'",
            event.vacancyId,
            event.title,
            event.employerCompanyName,
        )
        val url = buildJcaGatewayDealAddUrl() ?: return
        val vacancyUrl = buildPublicVacancyUrl(event)
        val body = buildDealAddBody(event, vacancyUrl)
        log.debug(
            "Bitrix outbound prepared: vacancyId={} url={} fields={}",
            event.vacancyId,
            url,
            body["fields"]?.keys?.joinToString(","),
        )
        postDealAdd(event, url, body)
    }

    private fun buildJcaGatewayDealAddUrl(): String? {
        val base = properties.gatewayBaseUrl
        if (base == null || !hasText(base)) {
            log.debug(
                "Bitrix JCA: gateway base URL not set, skip outbound; set blps.eis.bitrix-jca.gateway-base-url",
            )
            return null
        }
        val trimmed = base.trim().trimEnd('/')
        val path = properties.crmDealAddPath.trim()
        val suffix = if (path.startsWith("/")) path else "/$path"
        return "$trimmed$suffix"
    }

    private fun buildPublicVacancyUrl(event: VacancyPublishedForBitrixCommitted): String {
        val publicBase = (properties.vacancyApiPublicBaseUrl ?: "").trimEnd()
        val vacancyPath = "${properties.vacancyApiPathPrefix.trimEnd('/')}/${event.vacancyId}"
        return if (hasText(publicBase)) {
            "$publicBase$vacancyPath"
        } else {
            "(vacancy public base not configured, blps.eis.bitrix-jca.vacancy-api-public-base-url)"
        }
    }

    private fun buildDealAddBody(
        event: VacancyPublishedForBitrixCommitted,
        vacancyUrl: String,
    ): Map<String, Map<String, Any>> {
        val comments = buildString {
            appendLine("Работодатель: ${event.employerCompanyName}")
            appendLine("Тариф: ${event.tariffName}, срок: ${event.tariffDurationDays} дн.")
            appendLine("Вакансия в API: $vacancyUrl")
        }
        val fields = mutableMapOf<String, Any>(
            "TITLE" to "Вакансия: ${event.title}",
            "COMMENTS" to comments.trimEnd(),
        )
        putOptionalField(fields, properties.vacancyIdFieldCode, event.vacancyId.toString())
        putOptionalField(fields, properties.vacancyUrlFieldCode, vacancyUrl)
        putOptionalField(fields, properties.employerFieldCode, event.employerCompanyName)
        putOptionalField(fields, properties.tariffNameFieldCode, event.tariffName)
        putOptionalField(fields, properties.tariffDurationDaysFieldCode, event.tariffDurationDays)
        properties.dealCategoryId?.let { fields["CATEGORY_ID"] = it }
        val stageId = properties.initialStageId
        if (stageId != null && hasText(stageId)) {
            fields["STAGE_ID"] = stageId.trim()
        }
        return mapOf(
            "fields" to fields,
        )
    }

    private fun postDealAdd(
        event: VacancyPublishedForBitrixCommitted,
        url: String,
        body: Map<String, Map<String, Any>>,
    ) {
        val startedAtNanos = System.nanoTime()
        try {
            val raw = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String::class.java) ?: ""
            val elapsedMs = (System.nanoTime() - startedAtNanos) / 1_000_000
            log.debug(
                "Bitrix gateway responded: vacancyId={} elapsedMs={} bodySnippet={}",
                event.vacancyId,
                elapsedMs,
                shorten(raw),
            )
            val node = objectMapper.readTree(raw)
            handleDealAddResponse(event, raw, node)
        } catch (e: RestClientException) {
            val elapsedMs = (System.nanoTime() - startedAtNanos) / 1_000_000
            log.error(
                "Bitrix JCA gateway request failed: vacancyId={} url={} elapsedMs={}",
                event.vacancyId,
                url,
                elapsedMs,
                e,
            )
        } catch (e: JacksonException) {
            val elapsedMs = (System.nanoTime() - startedAtNanos) / 1_000_000
            log.error(
                "Bitrix JCA gateway response parse failed: vacancyId={} elapsedMs={} raw={}",
                event.vacancyId,
                elapsedMs,
                shorten(body["fields"].toString()),
                e,
            )
        }
    }

    private fun handleDealAddResponse(
        event: VacancyPublishedForBitrixCommitted,
        raw: String,
        node: JsonNode,
    ) {
        if (node.hasNonNull("error") && !node.get("error").asText("").isEmpty()) {
            val desc = if (node.has("error_description")) {
                node.get("error_description").asText("?")
            } else {
                "?"
            }
            log.error(
                "Bitrix RAR returned error: error={} desc={} vacancyId={} raw={}",
                node.get("error").asText(),
                desc,
                event.vacancyId,
                raw,
            )
            return
        }
        val result = node.get("result")
        if (result == null || result.isNull) {
            log.warn("Bitrix JCA gateway unexpected body: {} vacancyId={}", raw, event.vacancyId)
            return
        }
        val dealIdForLog = if (result.isValueNode) result.asText() else result.toString()
        log.info(
            "Bitrix deal created (via JCA stack): dealId={} vacancyId={}",
            dealIdForLog,
            event.vacancyId,
        )
    }

    private fun shorten(raw: String, max: Int = 500): String {
        return if (raw.length <= max) raw else raw.substring(0, max) + "...(truncated)"
    }

    private fun putOptionalField(
        fields: MutableMap<String, Any>,
        fieldCode: String?,
        value: Any,
    ) {
        if (fieldCode != null && hasText(fieldCode)) {
            fields[fieldCode.trim()] = value
        }
    }
}
