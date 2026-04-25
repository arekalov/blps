package com.arekalov.blps.kafka

import com.arekalov.blps.service.VacancyModerationEnqueueService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class VacancySubmissionKafkaListener(
    private val moderationEnqueueService: VacancyModerationEnqueueService,
) {

    @KafkaListener(
        topics = ["\${blps.kafka.vacancy-submitted-topic}"],
        groupId = "\${spring.kafka.consumer.group-id}",
    )
    fun onVacancySubmitted(value: String) {
        moderationEnqueueService.acceptSubmittedPayload(value)
    }
}
