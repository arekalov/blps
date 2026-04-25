package com.arekalov.blps.kafka

import com.arekalov.blps.kafka.dto.VacancySubmittedForModerationMessage
import com.arekalov.blps.kafka.event.VacancySubmittedForModerationCommitted
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class VacancySubmittedKafkaPublisher(
    private val producer: KafkaProducer<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${blps.kafka.vacancy-submitted-topic}") private val topic: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onVacancySubmittedCommitted(event: VacancySubmittedForModerationCommitted) {
        val payload = VacancySubmittedForModerationMessage(
            eventId = event.eventId,
            vacancyId = event.vacancyId,
            employerId = event.employerId,
            occurredAt = event.occurredAt,
        )
        val json = objectMapper.writeValueAsString(payload)
        val record = ProducerRecord(topic, event.vacancyId.toString(), json)
        producer.send(record) { _, exception ->
            if (exception != null) {
                log.error(
                    "Kafka send failed after DB commit for vacancy submission: vacancyId={} eventId={}",
                    event.vacancyId,
                    event.eventId,
                    exception,
                )
            }
        }
    }
}
