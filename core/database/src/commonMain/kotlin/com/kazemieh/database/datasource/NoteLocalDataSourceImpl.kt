package com.kazemieh.database.datasource

import com.kazemieh.common.model.Note
import com.kazemieh.data_contract.datasource.NoteLocalDataSource
import com.kazemieh.database.FinTrackDb
import com.kazemieh.database.mapper.toNote
import com.kazemieh.database.mapper.toTag
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.datetime.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteLocalDataSourceImpl(
    private val db: FinTrackDb
) : NoteLocalDataSource {

    private val noteQueries = db.noteQueries
    private val noteTagQueries = db.noteTagQueries

    override fun observeNotes(): Flow<List<Note>> {
        return noteQueries.observeNotes()
            .asFlow()
            .mapToList(Dispatchers.IO)
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
                updatedAt = now,
                syncStatus = 1
            )
            val noteId = noteQueries.lastInsertRowId().executeAsOne()
            note.tags.forEach { tag ->
                noteTagQueries.insertNoteTagCrossRef(noteId, tag.id)
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
                updatedAt = now,
                syncStatus = 1,
                id = note.id
            )
            noteTagQueries.deleteAllTagRefsForNote(note.id)
            note.tags.forEach { tag ->
                noteTagQueries.insertNoteTagCrossRef(note.id, tag.id)
            }
        }
    }

    override suspend fun deleteNote(id: Long) {
        noteQueries.deleteNote(id)
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
                updatedAt = note.updatedAt,
                syncStatus = note.syncStatus.value.toLong()
            )
            noteTagQueries.deleteAllTagRefsForNote(note.id)
            note.tags.forEach { tag ->
                noteTagQueries.insertNoteTagCrossRef(note.id, tag.id)
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
}
