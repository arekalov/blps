package com.arekalov.blps.mapper

import com.arekalov.blps.dto.common.PagedResponse
import org.springframework.data.domain.Page

fun <T, R> Page<T>.toPagedResponse(mapper: (T) -> R): PagedResponse<R> {
    return PagedResponse(
        content = content.map(mapper),
        page = number,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )
}
