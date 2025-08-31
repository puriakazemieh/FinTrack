package com.kazemieh.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Long?,
    val name: String
)