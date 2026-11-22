package com.kazemieh.domain.notification

import com.kazemieh.common.model.Note
import com.kazemieh.common.model.SyncStatus

/** Resolve old jobs conservatively; an integer hash must never select an ambiguous record. */
fun resolveNoteReminder(
    notes: List<Note>,
    recordId: Long?,
    notificationId: Int,
    scheduledTimeMillis: Long?,
    nowMillis: Long
): Note? {
    if (recordId != null && recordId <= 0) return null
    val active = notes.filter { it.id > 0 && it.syncStatus != SyncStatus.DELETED }
    val note = if (recordId != null) active.singleOrNull { it.id == recordId }
    else active.singleOrNull { "note_${it.id}".hashCode() == notificationId }
    val due = note?.reminderTime ?: return null
    if (due > nowMillis || (scheduledTimeMillis != null && due != scheduledTimeMillis)) return null
    return note
}
