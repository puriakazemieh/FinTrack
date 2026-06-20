package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Note
import com.kazemieh.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class ObserveNotesUseCase(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repository.observeNotes()
}

class GetNoteByIdUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Long): Note? = repository.getNoteById(id)
}

class AddNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(note: Note): Long = repository.addNote(note)
}

class UpdateNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(note: Note) = repository.updateNote(note)
}

class DeleteNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteNote(id)
}

class ToggleNotePinUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Long, isPinned: Boolean) = repository.togglePin(id, isPinned)
}

class ToggleNoteLockUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Long, isLocked: Boolean) = repository.toggleLock(id, isLocked)
}
