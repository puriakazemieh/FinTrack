package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class Tag(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val colorId: Int,
    val iconId: Int,
    val position: Int = 0
)