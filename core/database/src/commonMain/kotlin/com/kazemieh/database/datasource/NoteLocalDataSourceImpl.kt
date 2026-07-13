package com.kazemieh.database.datasource

import com.kazemieh.common.model.Note
import com.kazemieh.data_contract.datasource.NoteLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.mapper.toNote
import com.kazemieh.database.mapper.toTag
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

class NoteLocalDataSourceImpl(
    private val db: FinTrackDatabase
) : NoteLocalDataSource {

    private val noteQueries = db.noteQueries
    private val noteTagQueries = db.noteTagQueries

    override fun observeNotes(): Flow<List<Note>> {
        return noteQueries.observeNotes()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list ->
                list.map { noteDb ->
                    val tags = noteTagQueries.getTagsForNote(noteDb.id).executeAsList().map { it.toTag() }
                    noteDb.toNote(tags)
                }
            }
    }

    override suspend fun getNoteById(id: Long): Note? {
        val noteDb = noteQueries.getNoteById(id).executeAsOneOrNull() ?: return null
        val tags = noteTagQueries.getTagsForNote(id).executeAsList().map { it.toTag() }
        return noteDb.toNote(tags)
    }

    override suspend fun addNote(note: Note): Long {
        return db.transactionWithResult {
            val now = Clock.System.now().toEpochMilliseconds()
            noteQueries.addNote(
                title = note.title,
                content = note.content,
                color = note.color,
                isPinned = note.isPinned,
                isLocked = note.isLocked,
                reminderTime = note.reminderTime,
                updatedAt = now,
                syncStatus = 1
            )
            val noteId = noteQueries.lastInsertRowId().executeAsOne()
            note.tags.forEach { tag ->
                noteTagQueries.insertNoteTagCrossRef(noteId, tag.id ?: 0)
            }
            noteId
        }
    }

    override suspend fun updateNote(note: Note) {
        db.transaction {
            val now = Clock.System.now().toEpochMilliseconds()
            noteQueries.updateNote(
                title = note.title,
                content = note.content,
                color = note.color,
                isPinned = note.isPinned,
                isLocked = note.isLocked,
                reminderTime = note.reminderTime,
                updatedAt = now,
                syncStatus = 1,
                id = note.id
            )
            noteTagQueries.deleteAllTagRefsForNote(note.id)
            note.tags.forEach { tag ->
                noteTagQueries.insertNoteTagCrossRef(note.id, tag.id ?: 0)
            }
        }
    }

    override suspend fun deleteNote(id: Long) {
        val now = Clock.System.now().toEpochMilliseconds()
        noteQueries.deleteNote(now, id)
    }

    override suspend fun updatePin(id: Long, isPinned: Boolean) {
        val now = Clock.System.now().toEpochMilliseconds()
        noteQueries.updatePin(isPinned, now, 1, id)
    }

    override suspend fun updateLock(id: Long, isLocked: Boolean) {
        val now = Clock.System.now().toEpochMilliseconds()
        noteQueries.updateLock(isLocked, now, 1, id)
    }

    override suspend fun getAllNotes(): List<Note> {
        return noteQueries.observeNotes().executeAsList().map { noteDb ->
            val tags = noteTagQueries.getTagsForNote(noteDb.id).executeAsList().map { it.toTag() }
            noteDb.toNote(tags)
        }
    }

    override suspend fun insertFullNote(note: Note) {
        db.transaction {
            noteQueries.insertFullNote(
                id = note.id,
                title = note.title,
                content = note.content,
                color = note.color,
                isPinned = note.isPinned,
                isLocked = note.isLocked,
                reminderTime = note.reminderTime,
                updatedAt = note.updatedAt,
                syncStatus = note.syncStatus.value.toLong()
            )
            noteTagQueries.deleteAllTagRefsForNote(note.id)
            note.tags.forEach { tag ->
                noteTagQueries.insertNoteTagCrossRef(note.id, tag.id ?: 0)
            }
        }
    }

    override suspend fun getModifiedNotes(): List<Note> {
        return noteQueries.getModifiedNotes().executeAsList().map { noteDb ->
            val tags = noteTagQueries.getTagsForNote(noteDb.id).executeAsList().map { it.toTag() }
            noteDb.toNote(tags)
        }
    }

    override suspend fun markNoteAsSynced(id: Long) {
        noteQueries.markNoteAsSynced(id)
    }

    override suspend fun physicallyDeleteNote(id: Long) {
        noteQueries.physicallyDeleteNote(id)
    }
}
