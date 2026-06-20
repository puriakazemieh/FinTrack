package com.kazemieh.domain.repository

import com.kazemieh.common.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * رابط کاربری برای مدیریت داده‌های یادداشت‌ها.
 */
interface NoteRepository {
    fun observeNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note?
    suspend fun addNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: Long)
    suspend fun togglePin(id: Long, isPinned: Boolean)
    suspend fun toggleLock(id: Long, isLocked: Boolean)
}
