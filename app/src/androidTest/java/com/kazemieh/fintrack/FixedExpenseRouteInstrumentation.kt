package com.kazemieh.fintrack

import android.app.Instrumentation
import android.app.NotificationManager as SystemNotifications
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.domain.notification.canonicalNotificationChannel
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.domain.usecase.FixedExpenseUseCaseGroup
import com.kazemieh.domain.usecase.GetDefaultCategoryUseCase
import com.kazemieh.domain.usecase.GetDefaultFinancialSourceUseCase
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.fixed_expense_not_found
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import org.koin.java.KoinJavaComponent.getKoin

/** Opt-in emulator probe for actual fixed-expense notification PendingIntents. */
class FixedExpenseRouteInstrumentation : Instrumentation() {
    private var arguments = Bundle()
    override fun onCreate(arguments: Bundle?) { this.arguments = arguments ?: Bundle(); super.onCreate(arguments); start() }
    override fun onStart() {
        val ids = mutableListOf<Long>()
        val failures = mutableListOf<String>()
        var tests = 0
        var retainPrepared = false
        var cases: FixedExpenseUseCaseGroup? = null
        var publisher: NotificationManager? = null
        var scheduler: NotificationScheduler? = null
        val mode = arguments.getString("mode") ?: "test"
        val marker = "QA-FIXED-ROUTE-${System.currentTimeMillis()}"
        val output = File(targetContext.filesDir, "qa-fixed-route").apply { mkdirs() }
        try {
            check(Build.HARDWARE.contains("ranchu") || Build.HARDWARE.contains("goldfish")) { "Emulator only" }
            var repository: FixedExpenseRepository? = null
            repeat(100) {
                if (repository == null) repository = runCatching { getKoin().get<FixedExpenseRepository>() }.getOrNull()
                if (repository == null) Thread.sleep(100)
            }
            val records = checkNotNull(repository)
            cases = getKoin().get<FixedExpenseUseCaseGroup>()
            publisher = getKoin().get<NotificationManager>()
            scheduler = getKoin().get<NotificationScheduler>()
            if (mode == "cleanup") {
                val requested = arguments.getString("recordIds")!!.split(',').map(String::toLong)
                check(requested.size in 1..3 && requested.all { it > 4L })
                requested.forEach { id ->
                    val owned = runBlocking { records.getFixedExpenseById(id) }
                    check(owned != null && owned.title.startsWith("QA-FIXED-ROUTE-")) { "Foreign cleanup record $id" }
                    ids += id
                }
            } else {
                publisher.createChannels()
                check(publisher.hasPermission()) { "Notification permission required" }
                check(!(targetContext.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager).isKeyguardLocked)
                val category = runBlocking { getKoin().get<GetDefaultCategoryUseCase>()(TransactionType.EXPENSE).id }!!
                val source = runBlocking { getKoin().get<GetDefaultFinancialSourceUseCase>()()?.id }!!
                val due = System.currentTimeMillis() + 4000L
                for ((index, suffix) in listOf("A", "B", "C").withIndex()) {
                    val model = FixedExpense(title = "$marker-$suffix", amount = (index + 1) * 1000L,
                        categoryId = category, sourceId = source, description = "$marker-detail-$suffix",
                        recurrence = RecurrenceType.MONTHLY, startDate = due, nextDueDate = due + 86400000L,
                        reminderEnabled = true, reminderTime = due, isAutoPostEnabled = false)
                    val id = runBlocking { records.insertFixedExpense(model) }
                    ids += id
                    scheduler.scheduleReminder("fixed_expense_$id", "QA reminder", model.title,
                        Instant.fromEpochMilliseconds(due).toLocalDateTime(TimeZone.currentSystemDefault()),
                        NotificationManager.CHANNEL_FIXED_EXPENSE, id)
                }
                val system = targetContext.getSystemService(Context.NOTIFICATION_SERVICE) as SystemNotifications
                val notificationIds = ids.map { "fixed_expense_$it".hashCode() }
                await("three real Worker cards") {
                    system.activeNotifications.count { it.id in notificationIds &&
                        canonicalNotificationChannel(it.notification.channelId) == NotificationManager.CHANNEL_FIXED_EXPENSE } == 3
                }
                val cards = notificationIds.map { id -> system.activeNotifications.single { it.id == id } }
                if (mode == "prepare") {
                    retainPrepared = true
                } else {
                    check(mode == "test")
                    fun test(name: String, block: () -> Unit) {
                        tests++
                        try { block() } catch (error: Throwable) { failures += "$name: ${error.stackTraceToString()}"; runCatching { capture(output, "$name-failed") } }
                    }
                    test("initial") {
                        cards[1].notification.contentIntent.send()
                        await("second record editor from initial Activity delivery") { editing("$marker-B") && !editing("$marker-A") }
                        capture(output, "initial")
                        check(system.activeNotifications.any { it.id == notificationIds[0] }) { "First card was cancelled" }
                    }
                    test("warm") {
                        cards[0].notification.contentIntent.send()
                        await("first record editor from warm delivery") { editing("$marker-A") && !editing("$marker-B") }
                        capture(output, "warm")
                    }
                    test("deleted") {
                        runBlocking { records.deleteFixedExpense(ids[2]) }
                        check(system.activeNotifications.any { it.id == notificationIds[2] }) { "Deleted fixture card disappeared before opening" }
                        cards[2].notification.contentIntent.send()
                        val message = runBlocking { getString(Res.string.fixed_expense_not_found) }
                        await("unavailable editor") { texts().contains(message) }
                        capture(output, "deleted")
                        check(nodes().none { it.className?.toString() == "android.widget.EditText" }) { "Old inputs remained" }
                    }
                    test("generic") {
                        targetContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("fintrack://fixed-expense"))
                            .setClass(targetContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                        await("original generic list") { texts().contains("هزینه‌های ثابت") && !texts().contains("ذخیره") }
                        capture(output, "generic")
                    }
                }
            }
        } catch (error: Throwable) { failures += "setup: ${error.stackTraceToString()}" }
        finally {
            if (!retainPrepared) ids.forEach { id ->
                runCatching { scheduler?.cancelReminder("fixed_expense_$id") }
                runCatching { publisher?.cancelNotification("fixed_expense_$id".hashCode(), NotificationManager.CHANNEL_FIXED_EXPENSE, id) }
                runCatching { runBlocking { cases?.deleteFixedExpenseUseCase?.invoke(id) } }
            }
        }
        val success = failures.isEmpty() && (mode != "test" || tests == 4)
        finish(if (success) -1 else 0, Bundle().apply {
            putInt("tests", tests); putInt("failures", failures.size); putString("failure_details", failures.joinToString("\n"))
            putString("record_ids", ids.joinToString(",")); putString("marker", marker); putBoolean("prepared", retainPrepared)
        })
    }
    private fun nodes(): List<AccessibilityNodeInfo> {
        val all = mutableListOf<AccessibilityNodeInfo>()
        fun visit(node: AccessibilityNodeInfo?) { if (node == null) return; all += node; for (i in 0 until node.childCount) visit(node.getChild(i)) }
        visit(uiAutomation.rootInActiveWindow); return all
    }
    private fun texts() = nodes().mapNotNull { it.text?.toString() }.joinToString("\n")
    private fun editing(title: String) = nodes().any { it.className?.toString() == "android.widget.EditText" && it.text?.toString() == title }
    private fun await(name: String, condition: () -> Boolean) { repeat(120) { if (condition()) return; Thread.sleep(100) }; error("Timed out: $name; ${texts()}") }
    private fun capture(directory: File, name: String) {
        Thread.sleep(500) // Capture the settled bottom-sheet frame, after its enter animation.
        File(directory, "$name.txt").writeText(texts())
        File(directory, "$name.png").outputStream().use { uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
