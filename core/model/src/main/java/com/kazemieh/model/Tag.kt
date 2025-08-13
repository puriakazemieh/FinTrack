package com.kazemieh.model

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: Long?,
    val name: String
)