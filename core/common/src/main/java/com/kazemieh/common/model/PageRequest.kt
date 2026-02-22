package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
data class PageRequest(
    val limit: Int,
    val offset: Int,
)

@Serializable
data class Page<T>(
    val items: List<T>,
    val request: PageRequest,
    val totalCount: Long? = null
)