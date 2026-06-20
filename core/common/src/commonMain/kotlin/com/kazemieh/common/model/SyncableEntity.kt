package com.kazemieh.common.model

interface SyncableEntity {
    val updatedAt: Long
    val syncStatus: SyncStatus
}
