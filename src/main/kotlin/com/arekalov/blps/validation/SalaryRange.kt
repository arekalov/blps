package com.arekalov.blps.validation

import java.math.BigDecimal

interface SalaryRange {
    val salaryFrom: BigDecimal?
    val salaryTo: BigDecimal?
}
