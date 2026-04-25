package com.arekalov.blps.common

import com.arekalov.blps.exception.ValidationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

object PaginationConstants {
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PAGE_SIZE = 100
    const val MIN_PAGE_SIZE = 1
}

fun validateAndCreatePageable(
    page: Int?,
    size: Int?,
    sort: Sort = Sort.unsorted(),
): Pageable {
    val pageNumber = page ?: 0
    val pageSize = size ?: PaginationConstants.DEFAULT_PAGE_SIZE

    if (pageNumber < 0) {
        throw ValidationException("Page number must not be negative")
    }

    if (pageSize < PaginationConstants.MIN_PAGE_SIZE) {
        throw ValidationException("Page size must be at least ${PaginationConstants.MIN_PAGE_SIZE}")
    }

    if (pageSize > PaginationConstants.MAX_PAGE_SIZE) {
        throw ValidationException("Page size must not exceed ${PaginationConstants.MAX_PAGE_SIZE}")
    }

    return PageRequest.of(pageNumber, pageSize, sort)
}
