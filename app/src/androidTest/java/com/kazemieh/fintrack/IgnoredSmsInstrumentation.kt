package com.kazemieh.fintrack

import android.app.Instrumentation
import android.app.NotificationManager as SystemNotifications
import android.content.Context
import android.os.Bundle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.kazemieh.common.model.*
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.repository.SmsDraftRepository
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.usecase.RegisterSmsDraftsUseCase
import com.kazemieh.domain.usecase.SmsRegistrationStatus
import com.kazemieh.sms_reader.IgnoredSmsCleanupWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.getKoin
import java.util.concurrent.TimeUnit

/** Synthetic fixtures only. The host harness backs up and restores the emulator database. */
class IgnoredSmsInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }
    private fun await(check: () -> Boolean) {
        repeat(100) { if (check()) return; Thread.sleep(100) }
        error("Timed out waiting for acceptance condition")
    }
    override fun onStart() {
        await { runCatching { getKoin().get<SmsDraftRepository>() }.isSuccess }
        val drafts = getKoin().get<SmsDraftRepository>()
        val transactions = getKoin().get<TransactionRepository>()
        val register = getKoin().get<RegisterSmsDraftsUseCase>()
        val publisher = getKoin().get<NotificationManager>()
        val system = targetContext.getSystemService(Context.NOTIFICATION_SERVICE) as SystemNotifications
        val failures = mutableListOf<String>()
        var tests = 0
        val fixtureIds = mutableListOf<Long>()
        var sourceId = 0L
        var categoryId = 0L
        fun test(name: String, action: suspend () -> Unit) {
            tests++
            try { runBlocking { action() } } catch (failure: Throwable) { failures += "$name: $failure" }
        }
        try {
            runBlocking {
                sourceId = transactions.addSource(Source(name = "QA SMS06 source", balance = 1000000, currencyCode = "IRT", colorId = 0, iconId = 0))
                categoryId = transactions.addCategory(Category(name = "QA SMS06 category", type = TransactionType.EXPENSE, colorId = 0, iconId = 0))
            }
            fun draft(label: String, ignored: Long? = null, complete: Boolean = true) = SmsDraft(sender = "QA SMS06 $label",
                body = "بلوبانک برداشت 120,000 ریال کارت 2222 $label", amount = 120000, bankName = "Blu",
                type = TransactionType.EXPENSE, categoryId = if (complete) categoryId else null, sourceId = sourceId,
                timeStamp = System.currentTimeMillis(), date = "1405/09/02", ignoredAt = ignored)
            fun create(label: String, ignored: Long? = null, complete: Boolean = true): Long = runBlocking {
                drafts.addSmsDraft(draft(label, ignored, complete)).also { fixtureIds += it }
            }
            test("notification ignore is independent, repeatable and does not consume or post") {
                val first = create("ignored-ready")
                val second = create("pending")
                val before = transactions.getTransactionCount()
                publisher.showStickyNotification(first.toInt(), "QA SMS06 ignore", "QA synthetic bank draft", first)
                publisher.showStickyNotification(second.toInt(), "QA SMS06 pending", "QA synthetic bank draft", second)
                await { system.activeNotifications.any { it.id == first.toInt() } }
                val action = system.activeNotifications.single { it.id == first.toInt() }.notification.actions.last().actionIntent
                action.send()
                await { runBlocking { drafts.getSmsDraftById(first)?.ignoredAt != null } }
                val ignored = drafts.getSmsDraftById(first)!!
                check(!ignored.isUsed)
                check(drafts.getSmsDraftById(second)!!.ignoredAt == null)
                check(transactions.getTransactionCount() == before)
                val time = ignored.ignoredAt
                action.send()
                Thread.sleep(250)
                check(drafts.getSmsDraftById(first)!!.ignoredAt == time)
                await { system.activeNotifications.none { it.id == first.toInt() } }
                check(system.activeNotifications.any { it.id == second.toInt() })
                publisher.cancelNotification(second.toInt(), NotificationManager.CHANNEL_SMS, second)
            }
            test("dismiss callback retains ordinary draft") {
                val id = create("dismissed")
                publisher.showStickyNotification(id.toInt(), "QA SMS06 dismiss", "QA synthetic bank draft", id)
                await { system.activeNotifications.any { it.id == id.toInt() } }
                system.activeNotifications.single { it.id == id.toInt() }.notification.deleteIntent.send()
                Thread.sleep(250)
                check(drafts.getSmsDraftById(id)!!.ignoredAt == null)
                check(!drafts.getSmsDraftById(id)!!.isUsed)
                publisher.cancelNotification(id.toInt(), NotificationManager.CHANNEL_SMS, id)
            }
            test("ignored quick registration posts once and incomplete draft stays editable") {
                val id = create("post", System.currentTimeMillis())
                val before = transactions.getSourceById(sourceId)!!.balance
                val count = transactions.getTransactionCount()
                check(register.registerOne(id, "IRT").status == SmsRegistrationStatus.POSTED)
                check(register.registerOne(id, "IRT").status == SmsRegistrationStatus.UNAVAILABLE)
                check(transactions.getSourceById(sourceId)!!.balance == before - 12000)
                check(transactions.getTransactionCount() == count + 1)
                val incomplete = create("incomplete", System.currentTimeMillis(), complete = false)
                check(register.registerOne(incomplete, "IRT").status == SmsRegistrationStatus.INCOMPLETE)
                check(!drafts.getSmsDraftById(incomplete)!!.isUsed)
            }
            test("real cleanup worker is idempotent, offline eligible and preserves financial rows") {
                val expired = create("expired", System.currentTimeMillis() - SMS_IGNORED_RETENTION_MILLIS - 10000)
                val count = transactions.getTransactionCount()
                val balance = transactions.getSourceById(sourceId)!!.balance
                val manager = WorkManager.getInstance(targetContext)
                IgnoredSmsCleanupWorker.enqueuePeriodicWork(targetContext)
                val periodic = manager.getWorkInfosForUniqueWork(IgnoredSmsCleanupWorker.WORK_NAME).get(15, TimeUnit.SECONDS).single()
                IgnoredSmsCleanupWorker.enqueuePeriodicWork(targetContext)
                check(manager.getWorkInfosForUniqueWork(IgnoredSmsCleanupWorker.WORK_NAME).get(15, TimeUnit.SECONDS).single().id == periodic.id)
                repeat(2) {
                    val request = OneTimeWorkRequestBuilder<IgnoredSmsCleanupWorker>().addTag("QA_SMS06").build()
                    manager.enqueue(request).result.get(15, TimeUnit.SECONDS)
                    await { manager.getWorkInfoById(request.id).get(10, TimeUnit.SECONDS)?.state == WorkInfo.State.SUCCEEDED }
                }
                check(drafts.getSmsDraftById(expired) == null)
                android.database.sqlite.SQLiteDatabase.openDatabase(targetContext.getDatabasePath("fintrack.db").absolutePath,
                    null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY).use { db ->
                    db.rawQuery("SELECT count(*) FROM sms_draft WHERE id = ?", arrayOf(expired.toString())).use { cursor ->
                        check(cursor.moveToFirst() && cursor.getLong(0) == 0L) { "Worker did not physically remove expired draft" }
                    }
                }
                check(transactions.getTransactionCount() == count)
                check(transactions.getSourceById(sourceId)!!.balance == balance)
                check(drafts.observeUnusedSmsDrafts().first().none { it.id == expired })
            }
            test("full Long identity survives explicit ignore") {
                val small = create("large-id")
                val large = 4_294_967_296L + small
                android.database.sqlite.SQLiteDatabase.openDatabase(targetContext.getDatabasePath("fintrack.db").absolutePath,
                    null, android.database.sqlite.SQLiteDatabase.OPEN_READWRITE).use { db ->
                    db.execSQL("UPDATE sms_draft SET id = ? WHERE id = ?", arrayOf<Any>(large, small))
                }
                fixtureIds.remove(small); fixtureIds += large
                publisher.showStickyNotification(large.toInt(), "QA SMS06 Long", "QA synthetic bank draft", large)
                await { system.activeNotifications.any { it.id == large.toInt() } }
                system.activeNotifications.single { it.id == large.toInt() }.notification.actions.last().actionIntent.send()
                await { runBlocking { drafts.getSmsDraftById(large)?.ignoredAt != null } }
                check(drafts.getSmsDraftById(large)!!.id == large)
                check(!drafts.getSmsDraftById(large)!!.isUsed)
            }
            // Keep isolated fixtures available for actual Compose/UI acceptance before host restoration.
            create("ui-ready", System.currentTimeMillis())
            create("ui-incomplete", System.currentTimeMillis(), complete = false)
            val cold = create("cold-action")
            publisher.showStickyNotification(cold.toInt(), "QA SMS06 cold", "QA synthetic bank draft", cold)
        } catch (failure: Throwable) { failures += "setup: $failure" }
        finish(if (failures.isEmpty()) -1 else 0, Bundle().apply {
            putInt("tests", tests); putInt("failures", failures.size)
            putString("failure_details", failures.joinToString("\n"))
            putString("draft_ids", fixtureIds.joinToString(","))
            putLong("source_id", sourceId); putLong("category_id", categoryId)
        })
    }
}
