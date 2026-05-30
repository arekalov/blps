package com.arekalov.blps.camunda

import com.arekalov.blps.exception.ValidationException
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validator
import org.springframework.stereotype.Component

@Component
class CamundaDtoValidation(
    private val validator: Validator,
) {

    fun requireValid(dto: Any) {
        val violations = validator.validate(dto)
        if (violations.isEmpty()) return
        throw ValidationException(
            violations.joinToString("\n") { formatViolation(it) },
        )
    }

    private fun formatViolation(violation: ConstraintViolation<*>): String {
        val fieldName = violation.propertyPath.lastOrNull()?.name?.takeIf { it.isNotBlank() }
        val message = violation.message ?: "invalid value"
        return if (fieldName != null) {
            "$fieldName: $message"
        } else {
            message
        }
    }
}
