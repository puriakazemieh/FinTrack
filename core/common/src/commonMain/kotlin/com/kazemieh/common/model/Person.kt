package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
@OptIn(kotlinx.serialization.InternalSerializationApi::class)
data class Person(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val position: Int = 0,
    override val updatedAt: Long = 0,
    override val syncStatus: SyncStatus = SyncStatus.SYNCED
) : SyncableEntity
