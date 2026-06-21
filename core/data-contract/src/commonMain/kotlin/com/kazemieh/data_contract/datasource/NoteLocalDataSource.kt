package com.kazemieh.data_contract.datasource

import com.kazemieh.common.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteLocalDataSource {
    fun observeNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note?
    suspend fun addNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: Long)
    suspend fun updatePin(id: Long, isPinned: Boolean)
    suspend fun updateLock(id: Long, isLocked: Boolean)

    suspend fun getAllNotes(): List<Note>
    suspend fun insertFullNote(note: Note)
    suspend fun getModifiedNotes(): List<Note>
    suspend fun markNoteAsSynced(id: Long)
    suspend fun physicallyDeleteNote(id: Long)
}
