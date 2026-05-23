package com.arekalov.blps.delegate.vacancy.query

import com.arekalov.blps.camunda.CamundaFormVariables
import com.arekalov.blps.camunda.CamundaPresentation
import com.arekalov.blps.camunda.ProcessUserResolver
import com.arekalov.blps.model.enum.VacancyStatus
import com.arekalov.blps.repository.VacancyRepository
import com.arekalov.blps.service.ModerationService
import com.arekalov.blps.service.VacancyService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component("vacancyPickerDelegate")
class VacancyPickerDelegate(
    private val vacancyRepository: VacancyRepository,
    private val moderationService: ModerationService,
    private val vacancyService: VacancyService,
    private val processUserResolver: ProcessUserResolver,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val mode = (execution.getVariable("pickerMode") as? String)?.uppercase() ?: "MY"
        val options = when (mode) {
            "PENDING" -> {
                val page = moderationService.getPendingVacancies(PageRequest.of(0, 100))
                page.content.map {
                    it.id to CamundaPresentation.vacancyOptionLabel(it.id, it.title, it.status.name)
                }
            }
            "DRAFT" -> {
                val actor = processUserResolver.resolveActor(execution)
                vacancyRepository.findByEmployerIdAndStatus(actor.id!!, VacancyStatus.DRAFT, PageRequest.of(0, 100))
                    .content
                    .map { v ->
                        v.id.toString() to CamundaPresentation.vacancyOptionLabel(v.id.toString(), v.title)
                    }
            }
            "PUBLISHED" -> {
                val actor = processUserResolver.resolveActor(execution)
                vacancyRepository.findByEmployerIdAndStatus(actor.id!!, VacancyStatus.PUBLISHED, PageRequest.of(0, 100))
                    .content
                    .map { v ->
                        v.id.toString() to CamundaPresentation.vacancyOptionLabel(v.id.toString(), v.title)
                    }
            }
            "ALL" -> {
                vacancyService.getAllVacancies(null, PageRequest.of(0, 100))
                    .content
                    .map { v ->
                        v.id to CamundaPresentation.vacancyOptionLabel(v.id, v.title, v.status.name)
                    }
            }
            else -> {
                val actor = processUserResolver.resolveActor(execution)
                vacancyRepository.findByEmployerId(actor.id!!, PageRequest.of(0, 100))
                    .content
                    .map { v ->
                        v.id.toString() to CamundaPresentation.vacancyOptionLabel(
                            v.id.toString(),
                            v.title,
                            v.status.name,
                        )
                    }
            }
        }

        if (options.isEmpty()) {
            throw IllegalStateException("Нет вакансий для выбора (режим: $mode)")
        }

        CamundaFormVariables.setSelectOptions(
            execution,
            "vacancyOptions",
            CamundaPresentation.toSelectOptions(options),
        )
    }
}
