package com.kazemieh.data.repository

import com.kazemieh.common.model.Note
import com.kazemieh.data_contract.datasource.NoteLocalDataSource
import com.kazemieh.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class NoteRepositoryImpl(
    private val localDataSource: NoteLocalDataSource
) : NoteRepository {
    override fun observeNotes(): Flow<List<Note>> = localDataSource.observeNotes()
    override suspend fun getNoteById(id: Long): Note? = localDataSource.getNoteById(id)
    override suspend fun addNote(note: Note): Long = localDataSource.addNote(note)
    override suspend fun updateNote(note: Note) = localDataSource.updateNote(note)
    override suspend fun deleteNote(id: Long) = localDataSource.deleteNote(id)
    override suspend fun togglePin(id: Long, isPinned: Boolean) = localDataSource.updatePin(id, isPinned)
    override suspend fun toggleLock(id: Long, isLocked: Boolean) = localDataSource.updateLock(id, isLocked)
}
