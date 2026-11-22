package com.kazemieh.domain.notification

import com.kazemieh.common.model.Note
import com.kazemieh.common.model.SyncStatus
import kotlin.test.*

class NoteReminderDeliveryTest {
    private val note = Note(id = 42, title = "Current title", content = "Current body", reminderTime = 1_000)
    private fun resolve(notes: List<Note> = listOf(note), id: Long? = 42, hash: Int = "note_42".hashCode(), scheduled: Long? = 1_000, now: Long = 2_000) =
        resolveNoteReminder(notes, id, hash, scheduled, now)

    @Test fun currentDueRecordIsDelivered() { assertEquals(note, resolve()) }
    @Test fun missingRecordIsNotDelivered() { assertNull(resolve(emptyList())) }
    @Test fun softDeletedRecordIsNotDelivered() { assertNull(resolve(listOf(note.copy(syncStatus = SyncStatus.DELETED)))) }
    @Test fun removedReminderIsNotDelivered() { assertNull(resolve(listOf(note.copy(reminderTime = null)))) }
    @Test fun postponedReminderIsNotDeliveredEarly() { assertNull(resolve(listOf(note.copy(reminderTime = 5_000)))) }
    @Test fun oldJobCannotDeliverADifferentPastOccurrence() { assertNull(resolve(listOf(note.copy(reminderTime = 500)))) }
    @Test fun exactDeadlineIsEligible() { assertEquals(note, resolve(now = 1_000)) }
    @Test fun explicitIdNeverFallsBackToAnotherRecordsHash() { assertNull(resolve(id = 7)) }
    @Test fun invalidExplicitIdsDoNotBecomeLegacyJobs() { assertNull(resolve(id = 0)); assertNull(resolve(id = -1)) }
    @Test fun legacyJobsResolveOnlyUniqueActiveHashes() { assertEquals(note, resolve(id = null, scheduled = null)) }
    @Test fun legacyHashCollisionIsNotGuessedEvenWhenOnlyOneCandidateIsDue() {
        val first = note.copy(id = 5_942_400_272_365_632_431L)
        val second = note.copy(id = 1_238_714_706_934_782_301L, reminderTime = 5_000)
        val hash = "note_${first.id}".hashCode()
        assertEquals(hash, "note_${second.id}".hashCode())
        assertNull(resolve(listOf(first, second), id = null, hash = hash, scheduled = null))
    }
    @Test fun fullLongIdDisambiguatesALegacyHashCollision() {
        val first = note.copy(id = 5_942_400_272_365_632_431L)
        val second = note.copy(id = 1_238_714_706_934_782_301L)
        assertEquals(second, resolve(listOf(first, second), id = second.id))
    }
    @Test fun editedTextAndLockStateComeFromTheCurrentRecord() {
        val edited = note.copy(title = "Edited", isLocked = true)
        assertEquals(edited, resolve(listOf(edited)))
    }
}
