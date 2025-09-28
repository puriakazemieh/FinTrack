package com.kazemieh.model

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
)