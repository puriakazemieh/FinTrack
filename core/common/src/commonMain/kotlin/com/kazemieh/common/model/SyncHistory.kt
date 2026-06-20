package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
data class SyncHistory(
    val id: Long = 0,
    val timestamp: Long,
    val type: SyncType,
    val status: SyncResultStatus,
    val recordCount: Int,
    val errorMessage: String? = null
)

enum class SyncType {
    AUTO_DRIVE, AUTO_SERVER, MANUAL
}

enum class SyncResultStatus {
    SUCCESS, FAILED
}
