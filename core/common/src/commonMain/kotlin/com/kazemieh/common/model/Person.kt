package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
@OptIn(kotlinx.serialization.InternalSerializationApi::class)
data class Person(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val position: Int = 0,
    val updatedAt: Long = 0,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
