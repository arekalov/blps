package com.arekalov.blps.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [SalaryRangeValidator::class])
annotation class ValidSalaryRange(
    val message: String = "Salary 'from' must not be greater than salary 'to'",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
