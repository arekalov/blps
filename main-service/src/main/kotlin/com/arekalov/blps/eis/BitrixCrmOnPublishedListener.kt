package com.arekalov.blps.eis

import com.arekalov.blps.eis.event.VacancyPublishedForBitrixCommitted
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class BitrixCrmOnPublishedListener(
    private val bitrixCrmService: BitrixCrmService,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onVacancyPublishedCommitted(event: VacancyPublishedForBitrixCommitted) {
        bitrixCrmService.tryCreateDealForPublishedVacancy(event)
    }
}
