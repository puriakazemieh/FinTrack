package com.kazemieh.common.model

import kotlinx.serialization.Serializable

/**
 * مدل داده‌ای برای یادداشت‌ها.
 */
@Serializable
data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val color: Long = 0xFFFFFFFF, // پیش‌فرض سفید
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    override val updatedAt: Long = 0,
    override val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val tags: List<Tag> = emptyList()
) : SyncableEntity
