package com.arekalov.blps.service

import com.arekalov.blps.dto.vacancy.CreateVacancyRequest
import com.arekalov.blps.dto.vacancy.UpdateVacancyRequest
import com.arekalov.blps.dto.vacancy.VacancyResponse
import com.arekalov.blps.exception.ForbiddenException
import com.arekalov.blps.exception.NotFoundException
import com.arekalov.blps.exception.ValidationException
import com.arekalov.blps.mapper.toEntity
import com.arekalov.blps.mapper.toResponse
import com.arekalov.blps.model.Skill
import com.arekalov.blps.model.enum.UserRole
import com.arekalov.blps.model.enum.VacancyStatus
import com.arekalov.blps.repository.SkillRepository
import com.arekalov.blps.repository.TariffRepository
import com.arekalov.blps.repository.UserRepository
import com.arekalov.blps.repository.VacancyRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class VacancyService(
    private val vacancyRepository: VacancyRepository,
    private val userRepository: UserRepository,
    private val tariffRepository: TariffRepository,
    private val skillRepository: SkillRepository,
) {

    @Transactional(readOnly = true)
    fun getAllVacancies(status: VacancyStatus?, pageable: Pageable): Page<VacancyResponse> {
        val vacancies = if (status != null) {
            vacancyRepository.findByStatus(status, pageable)
        } else {
            vacancyRepository.findAll(pageable)
        }
        return vacancies.map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getVacancyById(id: UUID): VacancyResponse {
        val vacancy = vacancyRepository.findById(id).orElseThrow {
            NotFoundException("Vacancy with id $id not found")
        }
        return vacancy.toResponse()
    }

    @Transactional(readOnly = true)
    fun getMyVacancies(userId: UUID, status: VacancyStatus?, pageable: Pageable): Page<VacancyResponse> {
        val vacancies = if (status != null) {
            vacancyRepository.findByEmployerIdAndStatus(userId, status, pageable)
        } else {
            vacancyRepository.findByEmployerId(userId, pageable)
        }
        return vacancies.map { it.toResponse() }
    }

    @Transactional
    fun createVacancy(userId: UUID, request: CreateVacancyRequest): VacancyResponse {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val skills = getOrCreateSkills(request.additionalSkills)
        val vacancy = request.toEntity(user, skills)
        val savedVacancy = vacancyRepository.save(vacancy)

        return savedVacancy.toResponse()
    }

    @Transactional
    fun updateVacancy(
        userId: UUID,
        vacancyId: UUID,
        userRole: UserRole,
        request: UpdateVacancyRequest,
    ): VacancyResponse {
        val vacancy = vacancyRepository.findById(vacancyId).orElseThrow {
            NotFoundException("Vacancy with id $vacancyId not found")
        }

        if (userRole != UserRole.ADMIN && vacancy.employer.id != userId) {
            throw ForbiddenException("You don't have permission to update this vacancy")
        }

        applyVacancyUpdates(vacancy, request)
        vacancy.updatedAt = LocalDateTime.now()

        val updatedVacancy = vacancyRepository.save(vacancy)
        return updatedVacancy.toResponse()
    }

    @Transactional
    fun deleteVacancy(userId: UUID, vacancyId: UUID, userRole: UserRole) {
        val vacancy = vacancyRepository.findById(vacancyId).orElseThrow {
            NotFoundException("Vacancy with id $vacancyId not found")
        }

        if (userRole != UserRole.ADMIN && vacancy.employer.id != userId) {
            throw ForbiddenException("You don't have permission to delete this vacancy")
        }

        vacancyRepository.delete(vacancy)
    }

    @Transactional
    fun selectTariff(userId: UUID, vacancyId: UUID, tariffId: UUID, userRole: UserRole): VacancyResponse {
        val vacancy = vacancyRepository.findById(vacancyId).orElseThrow {
            NotFoundException("Vacancy with id $vacancyId not found")
        }

        if (userRole != UserRole.ADMIN && vacancy.employer.id != userId) {
            throw ForbiddenException("You don't have permission to modify this vacancy")
        }

        if (vacancy.status != VacancyStatus.DRAFT) {
            throw ValidationException("Can only select tariff for draft vacancies")
        }

        val tariff = tariffRepository.findById(tariffId).orElseThrow {
            NotFoundException("Tariff with id $tariffId not found")
        }

        vacancy.tariff = tariff
        vacancy.updatedAt = LocalDateTime.now()

        val updatedVacancy = vacancyRepository.save(vacancy)
        return updatedVacancy.toResponse()
    }

    @Transactional
    fun publishVacancy(userId: UUID, vacancyId: UUID, userRole: UserRole): VacancyResponse {
        val vacancy = vacancyRepository.findById(vacancyId).orElseThrow {
            NotFoundException("Vacancy with id $vacancyId not found")
        }

        if (userRole != UserRole.ADMIN && vacancy.employer.id != userId) {
            throw ForbiddenException("You don't have permission to publish this vacancy")
        }

        if (vacancy.status != VacancyStatus.DRAFT) {
            throw ValidationException("Vacancy is already published or archived")
        }

        if (vacancy.tariff == null) {
            throw ValidationException("Cannot publish vacancy without a tariff")
        }

        vacancy.status = VacancyStatus.PUBLISHED
        vacancy.publishedAt = LocalDateTime.now()
        vacancy.updatedAt = LocalDateTime.now()

        val publishedVacancy = vacancyRepository.save(vacancy)
        return publishedVacancy.toResponse()
    }

    @Transactional
    fun archiveVacancy(userId: UUID, vacancyId: UUID, userRole: UserRole): VacancyResponse {
        val vacancy = vacancyRepository.findById(vacancyId).orElseThrow {
            NotFoundException("Vacancy with id $vacancyId not found")
        }

        if (userRole != UserRole.ADMIN && vacancy.employer.id != userId) {
            throw ForbiddenException("You don't have permission to archive this vacancy")
        }

        vacancy.status = VacancyStatus.ARCHIVED
        vacancy.updatedAt = LocalDateTime.now()

        val archivedVacancy = vacancyRepository.save(vacancy)
        return archivedVacancy.toResponse()
    }

    private fun applyVacancyUpdates(vacancy: com.arekalov.blps.model.Vacancy, request: UpdateVacancyRequest) {
        request.title?.let { vacancy.title = it }
        request.description?.let { vacancy.description = it }
        request.experienceLevel?.let { vacancy.experienceLevel = it }
        request.salaryFrom?.let { vacancy.salaryFrom = it }
        request.salaryTo?.let { vacancy.salaryTo = it }
        request.employmentType?.let { vacancy.employmentType = it }
        request.workFormat?.let { vacancy.workFormat = it }
        request.employmentFormat?.let { vacancy.employmentFormat = it }
        request.workSchedule?.let { vacancy.workSchedule = it }
        request.city?.let { vacancy.city = it }
        request.address?.let { vacancy.address = it }
        request.companyDescription?.let { vacancy.companyDescription = it }
        request.additionalSkills?.let {
            vacancy.additionalSkills = getOrCreateSkills(it).toMutableList()
        }
    }

    private fun getOrCreateSkills(skillNames: List<String>): List<Skill> {
        if (skillNames.isEmpty()) return emptyList()

        val existingSkills = skillRepository.findByNameIn(skillNames)
        val existingSkillNames = existingSkills.map { it.name }.toSet()

        val newSkillNames = skillNames.filterNot { it in existingSkillNames }
        val newSkills = newSkillNames.map { Skill(name = it) }
        val savedNewSkills = skillRepository.saveAll(newSkills)

        return existingSkills + savedNewSkills
    }
}
