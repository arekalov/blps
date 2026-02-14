package com.arekalov.blps.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class SalaryRangeValidator : ConstraintValidator<ValidSalaryRange, SalaryRange> {

    override fun isValid(value: SalaryRange?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) {
            return true
        }

        val salaryFrom = value.salaryFrom
        val salaryTo = value.salaryTo

        if (salaryFrom == null || salaryTo == null) {
            return true
        }

        return salaryFrom <= salaryTo
    }
}
