package com.arekalov.blps.camunda

import com.arekalov.blps.dto.common.PagedResponse
import com.arekalov.blps.dto.tariff.TariffResponse
import com.arekalov.blps.dto.tariff.TariffStatisticsResponse
import com.arekalov.blps.dto.tariff.TariffUsageHistoryResponse
import com.arekalov.blps.dto.user.UserResponse
import com.arekalov.blps.dto.vacancy.VacancyResponse

object CamundaPresentation {

    fun toSelectOptions(items: List<Pair<String, String>>): List<Map<String, String>> =
        items.map { (value, label) -> mapOf("value" to value, "label" to label) }

    fun formatVacancy(v: VacancyResponse): String = formatKeyValueTable(
        listOf(
            "ID" to v.id.toString(),
            "Название" to v.title,
            "Статус" to v.status.name,
            "Город" to v.city,
            "Опыт" to v.experienceLevel.name,
            "Зарплата" to "${v.salaryFrom ?: "—"} – ${v.salaryTo ?: "—"}",
            "Работодатель" to v.employerId.toString(),
            "Тариф" to (v.tariffId?.toString() ?: "не выбран"),
            "Описание" to (v.description ?: "—"),
            "Причина отклонения" to (v.rejectionReason ?: "—"),
            "Создана" to v.createdAt.toString(),
            "Опубликована" to (v.publishedAt?.toString() ?: "—"),
        ),
    )

    fun formatVacancyList(page: PagedResponse<VacancyResponse>): String {
        if (page.content.isEmpty()) return "_Вакансии не найдены._"
        return pageHeader(page) + formatMarkdownTable(
            headers = listOf("ID", "Название", "Статус", "Город"),
            rows = page.content.map { v ->
                listOf(
                    shortId(v.id.toString()),
                    truncate(v.title),
                    v.status.name,
                    v.city,
                )
            },
        )
    }

    fun formatTariff(t: TariffResponse): String = formatKeyValueTable(
        listOf(
            "ID" to t.id.toString(),
            "Название" to t.name,
            "Цена" to "${t.price} руб.",
            "Срок" to "${t.durationDays} дн.",
            "Описание" to (t.description ?: "—"),
        ),
    )

    fun formatTariffList(page: PagedResponse<TariffResponse>): String {
        if (page.content.isEmpty()) return "_Тарифы не найдены._"
        return pageHeader(page) + formatMarkdownTable(
            headers = listOf("ID", "Название", "Цена", "Срок"),
            rows = page.content.map { t ->
                listOf(
                    shortId(t.id.toString()),
                    truncate(t.name),
                    "${t.price} руб.",
                    "${t.durationDays} дн.",
                )
            },
        )
    }

    fun formatUser(u: UserResponse): String = formatKeyValueTable(
        listOf(
            "ID" to u.id.toString(),
            "Email" to u.email,
            "Компания" to u.companyName,
            "Роль" to u.role,
        ),
    )

    fun formatUserList(page: PagedResponse<UserResponse>): String {
        if (page.content.isEmpty()) return "_Пользователи не найдены._"
        return pageHeader(page) + formatMarkdownTable(
            headers = listOf("ID", "Email", "Роль", "Компания"),
            rows = page.content.map { u ->
                listOf(
                    shortId(u.id.toString()),
                    truncate(u.email, 36),
                    u.role,
                    truncate(u.companyName),
                )
            },
        )
    }

    fun formatTariffStatistics(s: TariffStatisticsResponse): String = formatKeyValueTable(
        listOf(
            "Тариф" to "${s.tariffName} (${shortId(s.tariffId.toString())})",
            "Цена" to "${s.currentPrice} руб.",
            "Срок" to "${s.currentDurationDays} дн.",
            "Использований" to s.usageCount.toString(),
            "Выручка" to "${s.totalRevenue} руб.",
            "Первое использование" to (s.firstUsedAt?.toString() ?: "—"),
            "Последнее использование" to (s.lastUsedAt?.toString() ?: "—"),
        ),
    )

    fun formatUsageHistory(page: PagedResponse<TariffUsageHistoryResponse>): String {
        if (page.content.isEmpty()) return "_История использования пуста._"
        return pageHeader(page) + formatMarkdownTable(
            headers = listOf("Дата", "Вакансия", "Работодатель", "Цена"),
            rows = page.content.map { h ->
                listOf(
                    h.publishedAt.toString(),
                    truncate(h.vacancyTitle),
                    truncate(h.employerCompanyName),
                    "${h.price} руб.",
                )
            },
        )
    }

    fun formatMarkdownTable(headers: List<String>, rows: List<List<String>>): String {
        if (rows.isEmpty()) return "_Нет данных._"
        val safeHeaders = headers.map(::escapeCell)
        val safeRows = rows.map { row ->
            headers.indices.map { i -> escapeCell(row.getOrElse(i) { "" }) }
        }
        val headerLine = "| ${safeHeaders.joinToString(" | ")} |"
        val divider = "| ${safeHeaders.joinToString(" | ") { "---" }} |"
        val body = safeRows.joinToString("\n") { "| ${it.joinToString(" | ")} |" }
        return "$headerLine\n$divider\n$body"
    }

    fun formatKeyValueTable(pairs: List<Pair<String, String>>): String =
        formatMarkdownTable(
            headers = listOf("Поле", "Значение"),
            rows = pairs.map { (k, v) -> listOf(k, escapeCell(v).replace("\\|", "|")) },
        )

    private fun pageHeader(page: PagedResponse<*>): String =
        "**Страница ${page.page + 1}/${page.totalPages}**, всего: ${page.totalElements}\n\n"

    private fun escapeCell(value: String): String =
        value.replace("|", "\\|").replace("\n", " ").replace("\r", "")

    private fun truncate(value: String, max: Int = 40): String =
        if (value.length <= max) value else value.take(max - 1) + "…"

    private fun shortId(uuid: String): String =
        if (uuid.length <= 8) uuid else uuid.take(8) + "…"

    fun intVar(execution: org.camunda.bpm.engine.delegate.DelegateExecution, name: String, default: Int): Int {
        val raw = execution.getVariable(name) ?: return default
        return when (raw) {
            is Number -> raw.toInt()
            is String -> raw.toIntOrNull() ?: default
            else -> default
        }
    }

    fun boolVar(execution: org.camunda.bpm.engine.delegate.DelegateExecution, name: String): Boolean {
        val raw = execution.getVariable(name) ?: return false
        return when (raw) {
            is Boolean -> raw
            is String -> raw.equals("true", ignoreCase = true)
            else -> false
        }
    }
}
