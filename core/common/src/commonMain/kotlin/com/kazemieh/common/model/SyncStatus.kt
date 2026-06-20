package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
enum class SyncStatus(val value: Int) {
    SYNCED(0),
    MODIFIED(1),
    DELETED(2);

    companion object {
        fun fromInt(value: Int) = entries.find { it.value == value } ?: SYNCED
    }
}
