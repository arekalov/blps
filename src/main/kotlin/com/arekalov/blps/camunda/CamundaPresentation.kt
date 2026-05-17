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

    fun formatVacancy(v: VacancyResponse): String = buildString {
        appendLine("ID: ${v.id}")
        appendLine("Название: ${v.title}")
        appendLine("Статус: ${v.status}")
        appendLine("Город: ${v.city}")
        appendLine("Опыт: ${v.experienceLevel}")
        appendLine("Зарплата: ${v.salaryFrom ?: "—"} – ${v.salaryTo ?: "—"}")
        appendLine("Работодатель: ${v.employerId}")
        appendLine("Тариф: ${v.tariffId ?: "не выбран"}")
        appendLine("Описание: ${v.description}")
        v.rejectionReason?.let { appendLine("Причина отклонения: $it") }
        appendLine("Создана: ${v.createdAt}")
        v.publishedAt?.let { appendLine("Опубликована: $it") }
    }

    fun formatVacancyList(page: PagedResponse<VacancyResponse>): String {
        if (page.content.isEmpty()) return "Вакансии не найдены."
        val header = "Страница ${page.page + 1}/${page.totalPages}, всего: ${page.totalElements}\n"
        val body = page.content.joinToString("\n---\n") { v ->
            "${v.id} | ${v.title} | ${v.status} | ${v.city}"
        }
        return header + body
    }

    fun formatTariff(t: TariffResponse): String = buildString {
        appendLine("ID: ${t.id}")
        appendLine("Название: ${t.name}")
        appendLine("Цена: ${t.price} руб.")
        appendLine("Срок: ${t.durationDays} дн.")
        appendLine("Описание: ${t.description ?: "—"}")
    }

    fun formatTariffList(page: PagedResponse<TariffResponse>): String {
        if (page.content.isEmpty()) return "Тарифы не найдены."
        val header = "Страница ${page.page + 1}/${page.totalPages}, всего: ${page.totalElements}\n"
        val body = page.content.joinToString("\n") { t ->
            "${t.id} | ${t.name} | ${t.price} руб. / ${t.durationDays} дн."
        }
        return header + body
    }

    fun formatUser(u: UserResponse): String = buildString {
        appendLine("ID: ${u.id}")
        appendLine("Email: ${u.email}")
        appendLine("Компания: ${u.companyName}")
        appendLine("Роль: ${u.role}")
    }

    fun formatUserList(page: PagedResponse<UserResponse>): String {
        if (page.content.isEmpty()) return "Пользователи не найдены."
        val header = "Страница ${page.page + 1}/${page.totalPages}, всего: ${page.totalElements}\n"
        val body = page.content.joinToString("\n") { u ->
            "${u.id} | ${u.email} | ${u.role} | ${u.companyName}"
        }
        return header + body
    }

    fun formatTariffStatistics(s: TariffStatisticsResponse): String = buildString {
        appendLine("Тариф: ${s.tariffName} (${s.tariffId})")
        appendLine("Цена: ${s.currentPrice} руб., срок: ${s.currentDurationDays} дн.")
        appendLine("Использований: ${s.usageCount}")
        appendLine("Выручка: ${s.totalRevenue} руб.")
        s.firstUsedAt?.let { appendLine("Первое использование: $it") }
        s.lastUsedAt?.let { appendLine("Последнее использование: $it") }
    }

    fun formatUsageHistory(page: PagedResponse<TariffUsageHistoryResponse>): String {
        if (page.content.isEmpty()) return "История использования пуста."
        val header = "Страница ${page.page + 1}/${page.totalPages}, всего: ${page.totalElements}\n"
        val body = page.content.joinToString("\n") { h ->
            "${h.publishedAt} | ${h.vacancyTitle} | ${h.employerCompanyName} | ${h.price} руб."
        }
        return header + body
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
}
