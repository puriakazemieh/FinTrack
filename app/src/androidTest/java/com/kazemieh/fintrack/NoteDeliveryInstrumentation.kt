package com.kazemieh.fintrack

import android.app.Instrumentation
import android.app.Notification
import android.app.NotificationManager as SystemNotifications
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
import com.kazemieh.notifications.NotificationWorker
import com.kazemieh.common.model.Note
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.domain.notification.notificationTag
import com.kazemieh.domain.repository.NoteRepository
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.java.KoinJavaComponent.getKoin

/** Opt-in emulator probe for stored WorkManager inputs and current note state at delivery. */
class NoteDeliveryInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }

    override fun onStart() {
        val failures = mutableListOf<String>()
        val ids = mutableListOf<Long>()
        var tests = 0
        var repository: NoteRepository? = null
        var scheduler: NotificationScheduler? = null
        try {
            check(android.os.Build.HARDWARE in setOf("ranchu", "goldfish")) { "Emulator required" }
            for (attempt in 0 until 100) {
                repository = runCatching { getKoin().get<NoteRepository>() }.getOrNull()
                if (repository != null) break
                Thread.sleep(100)
            }
            val notes = checkNotNull(repository) { "DI unavailable" }
            val reminders = getKoin().get<NotificationScheduler>().also { scheduler = it }
            val publisher = getKoin().get<NotificationManager>()
            publisher.createChannels()
            check(publisher.hasPermission()) { "Notification permission required" }
            check(!(targetContext.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager).isKeyguardLocked)
            startActivitySync(Intent(targetContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            val system = targetContext.getSystemService(Context.NOTIFICATION_SERVICE) as SystemNotifications
            val work = WorkManager.getInstance(targetContext)
            fun scenario(name: String, legacy: Boolean = false, mutate: (Note) -> Unit, verify: (Note, List<android.service.notification.StatusBarNotification>) -> Unit) {
                tests++
                try {
                    val due = System.currentTimeMillis() + 3_000
                    val marker = "QA-DELIVERY-${System.nanoTime()}"
                    val id = runBlocking { notes.addNote(Note(title = "$marker-secret", content = "$marker-body", reminderTime = due)) }
                    ids += id
                    val note = runBlocking { checkNotNull(notes.getNoteById(id)) }
                    val key = "note_$id"
                    if (legacy) {
                        // Match the persisted input of the old scheduler: neither extraId nor due time.
                        work.enqueueUniqueWork(key, ExistingWorkPolicy.REPLACE,
                            OneTimeWorkRequestBuilder<NotificationWorker>().setInitialDelay(3, TimeUnit.SECONDS)
                                .setInputData(NotificationWorker.createInputData("$marker-old-title", note.title,
                                    NotificationManager.CHANNEL_NOTE, key.hashCode())).build())
                    } else {
                        reminders.scheduleReminder(key, "$marker-old-title", note.title,
                            Instant.fromEpochMilliseconds(due).toLocalDateTime(TimeZone.currentSystemDefault()),
                            NotificationManager.CHANNEL_NOTE, id)
                    }
                    mutate(note)
                    val deadline = System.currentTimeMillis() + 30_000
                    var finished: WorkInfo? = null
                    while (System.currentTimeMillis() < deadline) {
                        finished = work.getWorkInfosForUniqueWork(key).get(5, TimeUnit.SECONDS).singleOrNull()
                        if (finished?.state?.isFinished == true) break
                        Thread.sleep(150)
                    }
                    check(finished?.state == WorkInfo.State.SUCCEEDED) { "Real worker did not succeed: ${finished?.state}" }
                    Thread.sleep(200)
                    verify(note, system.activeNotifications.filter { it.id == key.hashCode() })
                } catch (failure: Throwable) { failures += "$name: ${failure.stackTraceToString()}" }
            }
            fun noCard(note: Note, cards: List<android.service.notification.StatusBarNotification>) {
                check(cards.isEmpty()) { "Obsolete reminder for ${note.id} was published" }
            }
            scenario("deleted note is not delivered", mutate = { runBlocking { notes.deleteNote(it.id) } }, verify = ::noCard)
            scenario("removed reminder is not delivered", mutate = { runBlocking { notes.updateNote(it.copy(reminderTime = null)) } }, verify = ::noCard)
            scenario("postponed reminder does not arrive at the previous time", mutate = { runBlocking { notes.updateNote(it.copy(reminderTime = it.reminderTime!! + 3_600_000)) } }, verify = ::noCard)
            scenario("old job does not deliver a different earlier occurrence", mutate = { runBlocking { notes.updateNote(it.copy(reminderTime = System.currentTimeMillis() - 1_000)) } }, verify = ::noCard)
            scenario("locking after enqueue hides old title and content", mutate = { runBlocking { notes.toggleLock(it.id, true) } }, verify = { note, cards ->
                val card = cards.single().notification
                check(!card.extras.getCharSequence(Notification.EXTRA_TITLE).toString().contains("QA-DELIVERY")) { "Saved title leaked" }
                check(!card.extras.getCharSequence(Notification.EXTRA_TEXT).toString().contains(note.title.substringBefore("-secret"))) { "Locked content leaked" }
            })
            scenario("edited note uses its current title", mutate = { runBlocking { notes.updateNote(it.copy(title = "QA-current-${it.id}")) } }, verify = { note, cards ->
                check(cards.single().notification.extras.getCharSequence(Notification.EXTRA_TEXT).toString() == "QA-current-${note.id}") { "Frozen message was delivered" }
            })
            scenario("legacy job resolves the full record ID", legacy = true, mutate = {}, verify = { note, cards ->
                check(cards.single().tag == notificationTag(NotificationManager.CHANNEL_NOTE, "note_${note.id}".hashCode(), note.id)) { "Legacy job lost the record ID" }
            })
        } catch (failure: Throwable) { failures += "setup: ${failure.stackTraceToString()}" }
        finally {
            ids.forEach { id ->
                runCatching { scheduler?.cancelReminder("note_$id") }
                runCatching { getKoin().get<NotificationManager>().cancelNotification("note_$id".hashCode(), NotificationManager.CHANNEL_NOTE, id) }
                // Legacy publishers used the hash in the tag; clean only this fixture's card.
                runCatching { getKoin().get<NotificationManager>().cancelNotification("note_$id".hashCode(), NotificationManager.CHANNEL_NOTE) }
                runCatching { runBlocking { repository?.deleteNote(id) } }
            }
        }
        finish(if (failures.isEmpty() && tests == 7) -1 else 0, Bundle().apply {
            putInt("tests", tests); putInt("failures", failures.size); putString("failure_details", failures.joinToString("\n"))
        })
    }
}
