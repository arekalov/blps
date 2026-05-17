package com.arekalov.blps.delegate.vacancy.query

import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.camunda.ProcessUserResolver
import com.arekalov.blps.model.enum.VacancyStatus
import com.arekalov.blps.repository.VacancyRepository
import com.arekalov.blps.service.ModerationService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component("vacancyPickerDelegate")
class VacancyPickerDelegate(
    private val vacancyRepository: VacancyRepository,
    private val moderationService: ModerationService,
    private val processUserResolver: ProcessUserResolver,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val mode = (execution.getVariable("pickerMode") as? String)?.uppercase() ?: "MY"
        val options = when (mode) {
            "PENDING" -> {
                val page = moderationService.getPendingVacancies(PageRequest.of(0, 100))
                page.content.map { it.id to "${it.title} (${it.status})" }
            }
            "DRAFT" -> {
                val actor = processUserResolver.resolveActor(execution)
                vacancyRepository.findByEmployerIdAndStatus(actor.id!!, VacancyStatus.DRAFT, PageRequest.of(0, 100))
                    .content
                    .map { v -> v.id.toString() to "${v.title} (DRAFT)" }
            }
            "PUBLISHED" -> {
                val actor = processUserResolver.resolveActor(execution)
                vacancyRepository.findByEmployerIdAndStatus(actor.id!!, VacancyStatus.PUBLISHED, PageRequest.of(0, 100))
                    .content
                    .map { v -> v.id.toString() to "${v.title} (PUBLISHED)" }
            }
            else -> {
                val actor = processUserResolver.resolveActor(execution)
                vacancyRepository.findByEmployerId(actor.id!!, PageRequest.of(0, 100))
                    .content
                    .map { v -> v.id.toString() to "${v.title} (${v.status})" }
            }
        }

        if (options.isEmpty()) {
            throw IllegalStateException("Нет вакансий для выбора (режим: $mode)")
        }

        execution.setVariable("vacancyOptions", CamundaPresentation.toSelectOptions(options))
    }
}
