package com.kazemieh.fintrack

import android.app.Instrumentation
import android.app.NotificationManager as SystemNotifications
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import com.kazemieh.common.model.Note
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.domain.notification.canonicalNotificationChannel
import com.kazemieh.domain.repository.NoteRepository
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.note_not_found
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import org.koin.java.KoinJavaComponent.getKoin
import java.io.File

/** Opt-in, emulator-only integration probe: real repositories, scheduler, Worker and Activity. */
class NoteReminderInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }

    override fun onStart() {
        val failures = mutableListOf<String>()
        val ids = mutableListOf<Long>()
        var repository: NoteRepository? = null
        var scheduler: NotificationScheduler? = null
        var tests = 0
        val output = File(targetContext.filesDir, "qa-note-route").apply { mkdirs() }
        listOf("first.txt", "first.png", "second.txt", "second.png", "deleted.txt", "deleted.png", "record-ids.txt")
            .forEach { File(output, it).delete() }
        try {
            check(SystemPropertiesCompat.isEmulator())
            check(!(targetContext.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager).isKeyguardLocked) {
                "Unlock the emulator before running note navigation tests"
            }
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
            startActivitySync(Intent(targetContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            val marker = "QA-NOTE-${System.currentTimeMillis()}"
            val due = System.currentTimeMillis() + 4_000
            val first = runBlocking { notes.addNote(Note(title = "$marker-first", content = "$marker-body-first", reminderTime = due)) }
            ids += first
            val second = runBlocking { notes.addNote(Note(title = "$marker-second", content = "$marker-body-second", reminderTime = due)) }
            ids += second
            ids.forEachIndexed { index, id ->
                reminders.scheduleReminder("note_$id", "QA note reminder", "$marker-${if (index == 0) "first" else "second"}",
                    Instant.fromEpochMilliseconds(due).toLocalDateTime(TimeZone.currentSystemDefault()),
                    NotificationManager.CHANNEL_NOTE, id)
            }
            val system = targetContext.getSystemService(Context.NOTIFICATION_SERVICE) as SystemNotifications
            val deliveryIds = ids.map { "note_$it".hashCode() }
            await("Both real Workers must deliver") {
                system.activeNotifications.count { it.id in deliveryIds && canonicalNotificationChannel(it.notification.channelId) == NotificationManager.CHANNEL_NOTE } == 2
            }
            val firstCard = system.activeNotifications.single { it.id == deliveryIds[0] }
            val secondCard = system.activeNotifications.single { it.id == deliveryIds[1] }
            fun test(name: String, block: () -> Unit) {
                tests++
                try { block() } catch (error: Throwable) { failures += "$name: ${error.message}" }
            }
            test("second reminder opens its own record") {
                secondCard.notification.contentIntent.send()
                await("Second note editor") { texts().contains("$marker-body-second") && !texts().contains("$marker-body-first") }
                capture(output, "second")
                check(system.activeNotifications.any { it.id == deliveryIds[0] }) { "First reminder was cancelled" }
            }
            test("first reminder opens its own record while the app is open") {
                firstCard.notification.contentIntent.send()
                await("First note editor") { texts().contains("$marker-body-first") && !texts().contains("$marker-body-second") }
                capture(output, "first")
            }
            test("deleted reminder shows an unavailable editor") {
                runBlocking { notes.deleteNote(first) }
                firstCard.notification.contentIntent.send()
                val message = runBlocking { getString(Res.string.note_not_found) }
                await("Unavailable note feedback") { texts().contains(message) }
                capture(output, "deleted")
                check(!texts().contains("$marker-body-first")) { "Deleted content remained visible" }
            }
            File(output, "record-ids.txt").writeText(ids.joinToString(","))
        } catch (failure: Throwable) { failures += "setup: ${failure.stackTraceToString()}" }
        finally {
            ids.forEach { id ->
                runCatching { scheduler?.cancelReminder("note_$id") }
                runCatching { getKoin().get<NotificationManager>().cancelNotification("note_$id".hashCode(), NotificationManager.CHANNEL_NOTE, id) }
                runCatching { runBlocking { repository?.deleteNote(id) } }
            }
        }
        val result = Bundle().apply {
            putInt("tests", tests); putInt("failures", failures.size)
            putString("failure_details", failures.joinToString("\n"))
        }
        finish(if (failures.isEmpty() && tests == 3) -1 else 0, result)
    }

    private fun texts(): String {
        fun visit(node: AccessibilityNodeInfo?, result: StringBuilder) {
            if (node == null) return
            node.text?.let { result.append(it).append('\n') }
            for (index in 0 until node.childCount) visit(node.getChild(index), result)
        }
        return StringBuilder().also { visit(uiAutomation.rootInActiveWindow, it) }.toString()
    }

    private fun capture(directory: File, name: String) {
        File(directory, "$name.txt").writeText(texts())
        File(directory, "$name.png").outputStream().use { uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun await(message: String, condition: () -> Boolean) {
        repeat(200) { if (condition()) return; Thread.sleep(200) }
        error("$message timed out; screen=${texts()}")
    }
}

// Build properties are sufficient here; the host additionally pins serial/name/API before running.
private object SystemPropertiesCompat {
    fun isEmulator(): Boolean = android.os.Build.FINGERPRINT.startsWith("generic") ||
        android.os.Build.HARDWARE in setOf("ranchu", "goldfish")
}
