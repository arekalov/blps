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

    /** Подпись в select: UUID и название (опционально статус). */
    fun vacancyOptionLabel(id: String, title: String, status: String? = null): String {
        val base = "$id — $title"
        return if (status.isNullOrBlank()) base else "$base ($status)"
    }

    fun formatVacancy(v: VacancyResponse): String =
        fitProcessVariable(formatKeyValueTable(vacancyFields(v)))

    fun formatVacancyList(page: PagedResponse<VacancyResponse>): String {
        if (page.content.isEmpty()) return "_Вакансии не найдены._"
        val table = formatMarkdownTable(
            headers = VACANCY_LIST_HEADERS,
            rows = page.content.map { v ->
                listOf(v.id, v.title, v.status.name, v.city)
            },
        )
        return fitProcessVariable(
            pageHeader(page) + table +
                "\n\n_Подробности — процесс «[GET] Вакансия по ID»._",
        )
    }

    fun formatTariff(t: TariffResponse): String =
        fitProcessVariable(formatKeyValueTable(tariffFields(t)))

    fun formatTariffList(page: PagedResponse<TariffResponse>): String {
        if (page.content.isEmpty()) return "_Тарифы не найдены._"
        val table = formatMarkdownTable(
            headers = TARIFF_LIST_HEADERS,
            rows = page.content.map { t ->
                listOf(t.id, t.name, "${t.price} руб.", "${t.durationDays} дн.")
            },
        )
        return fitProcessVariable(
            pageHeader(page) + table +
                "\n\n_Подробности — процесс «[GET] Тариф по ID»._",
        )
    }

    fun formatUser(u: UserResponse): String =
        fitProcessVariable(formatKeyValueTable(userFields(u)))

    fun formatUserList(page: PagedResponse<UserResponse>): String {
        if (page.content.isEmpty()) return "_Пользователи не найдены._"
        val table = formatMarkdownTable(
            headers = USER_LIST_HEADERS,
            rows = page.content.map { u ->
                listOf(u.id, u.email, u.role, u.companyName)
            },
        )
        return fitProcessVariable(
            pageHeader(page) + table +
                "\n\n_Подробности — процесс «[GET] Пользователь по ID» (ADMIN)._",
        )
    }

    fun formatTariffStatistics(s: TariffStatisticsResponse): String = fitProcessVariable(
        formatKeyValueTable(
            listOf(
                "ID тарифа" to s.tariffId,
                "Название" to s.tariffName,
                "Цена" to "${s.currentPrice} руб.",
                "Срок (дн.)" to s.currentDurationDays.toString(),
                "Использований" to s.usageCount.toString(),
                "Выручка" to "${s.totalRevenue} руб.",
                "Первое использование" to display(s.firstUsedAt),
                "Последнее использование" to display(s.lastUsedAt),
            ),
        ),
    )

    fun formatUsageHistory(page: PagedResponse<TariffUsageHistoryResponse>): String {
        if (page.content.isEmpty()) return "_История использования пуста._"
        val table = formatMarkdownTable(
            headers = USAGE_HISTORY_HEADERS,
            rows = page.content.map { h ->
                listOf(
                    display(h.publishedAt),
                    h.vacancyId,
                    h.vacancyTitle,
                    h.tariffName,
                    "${h.price} руб.",
                )
            },
        )
        return fitProcessVariable(pageHeader(page) + table)
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
            rows = pairs.map { (k, v) -> listOf(k, v) },
        )

    private fun vacancyFields(v: VacancyResponse): List<Pair<String, String>> = listOf(
        "ID" to v.id,
        "Название" to v.title,
        "Описание" to v.description,
        "Опыт" to v.experienceLevel.name,
        "Зарплата от" to display(v.salaryFrom),
        "Зарплата до" to display(v.salaryTo),
        "Тип занятости" to v.employmentType.name,
        "Формат работы" to v.workFormat.name,
        "Формат оформления" to v.employmentFormat.name,
        "График" to v.workSchedule.name,
        "Город" to v.city,
        "Адрес" to display(v.address),
        "О компании" to display(v.companyDescription),
        "Навыки" to displaySkills(v.additionalSkills),
        "Статус" to v.status.name,
        "Работодатель" to v.employerId,
        "Тариф" to display(v.tariffId),
        "Модератор" to display(v.moderatorId),
        "Модерировано" to display(v.moderatedAt),
        "Причина отклонения" to display(v.rejectionReason),
        "Создана" to display(v.createdAt),
        "Обновлена" to display(v.updatedAt),
        "Опубликована" to display(v.publishedAt),
    )

    private fun tariffFields(t: TariffResponse): List<Pair<String, String>> = listOf(
        "ID" to t.id,
        "Название" to t.name,
        "Цена" to "${t.price} руб.",
        "Срок (дн.)" to t.durationDays.toString(),
        "Описание" to t.description,
    )

    private fun userFields(u: UserResponse): List<Pair<String, String>> = listOf(
        "ID" to u.id,
        "Email" to u.email,
        "Компания" to u.companyName,
        "Роль" to u.role,
    )

    private fun displaySkills(skills: List<String>): String =
        if (skills.isEmpty()) "—" else skills.joinToString(", ")

    private fun display(value: Any?): String = when (value) {
        null -> "—"
        else -> value.toString()
    }

    private fun pageHeader(page: PagedResponse<*>): String =
        "**Страница ${page.page + 1}/${page.totalPages}**, всего: ${page.totalElements}\n\n"

    private fun escapeCell(value: String): String =
        value.replace("|", "\\|").replace("\n", " ").replace("\r", "")

    /** Лимит Camunda для строковых переменных процесса (ACT_RU_VARIABLE). */
    private fun fitProcessVariable(text: String, maxLength: Int = 3900): String {
        if (text.length <= maxLength) return text
        return text.take(maxLength - 100) +
            "\n\n_(вывод обрезан — уменьшите size или откройте запись по ID)_"
    }

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

    private val VACANCY_LIST_HEADERS = listOf("ID", "Название", "Статус", "Город")

    private val TARIFF_LIST_HEADERS = listOf("ID", "Название", "Цена", "Срок")

    private val USER_LIST_HEADERS = listOf("ID", "Email", "Роль", "Компания")

    private val USAGE_HISTORY_HEADERS = listOf(
        "Опубликована",
        "ID вакансии",
        "Вакансия",
        "Тариф",
        "Цена",
    )
}
