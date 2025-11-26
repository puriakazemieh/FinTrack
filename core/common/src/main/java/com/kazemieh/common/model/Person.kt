package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
data class Person(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
)