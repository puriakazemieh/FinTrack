package com.kazemieh.fintrack

import android.app.Instrumentation
import android.app.NotificationManager as SystemNotifications
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.work.*
import com.kazemieh.common.model.*
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.usecase.FixedExpenseUseCaseGroup
import com.kazemieh.domain.usecase.GetDefaultCategoryUseCase
import com.kazemieh.fixed_expense.FixedExpenseWorker
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.koin.java.KoinJavaComponent.getKoin

/** Persistent, emulator-only fixture. The host owns video, gestures and full snapshot recovery. */
class FixedExpenseNotificationAcceptanceInstrumentation : Instrumentation() {
    private var arguments = Bundle()
    override fun onCreate(arguments: Bundle?) {
        this.arguments = arguments ?: Bundle(); super.onCreate(arguments); start()
    }

    override fun onStart() {
        val failures = mutableListOf<String>()
        var report = JSONObject()
        val manifest = File(targetContext.filesDir, "qa-fixed-notification-owned.json")
        try {
            check(Build.HARDWARE.contains("ranchu") || Build.HARDWARE.contains("goldfish")) { "Emulator only" }
            check(Build.VERSION.SDK_INT == 31) { "Selected QA API31 only" }
            var repository: TransactionRepository? = null
            await("Koin") { repository = runCatching { getKoin().get<TransactionRepository>() }.getOrNull(); repository != null }
            val tx = checkNotNull(repository)
            val fixed = getKoin().get<FixedExpenseRepository>()
            val cases = getKoin().get<FixedExpenseUseCaseGroup>()
            val publisher = getKoin().get<NotificationManager>()
            val work = WorkManager.getInstance(targetContext)
            val mode = arguments.getString("mode") ?: "prepare"
            val delay = (arguments.getString("delaySeconds") ?: "45").toLong()
            require(delay in 10..180)
            var owned: JSONObject
            if (mode == "prepare") {
                check(!manifest.exists()) { "Unfinished fixture requires cleanup or snapshot recovery" }
                val marker = System.currentTimeMillis().toString()
                val source = runBlocking { tx.addSource(Source(name = "QA-N9-$marker", balance = 10000, colorId = 0, iconId = 0)) }
                owned = JSONObject().put("marker", marker).put("source", source).put("expenses", JSONArray())
                    .put("autoWork", "QA_N9_AUTO_$marker")
                manifest.writeText(owned.toString())
                val category = runBlocking { getKoin().get<GetDefaultCategoryUseCase>()(TransactionType.EXPENSE).id }!!
                val period = System.currentTimeMillis() - 60000
                for (kind in listOf("A", "B")) {
                    val title = "QA-N9-$marker-$kind"
                    runBlocking { cases.addFixedExpenseUseCase(FixedExpense(title = title, amount = 1000,
                        categoryId = category, sourceId = source, recurrence = RecurrenceType.MONTHLY,
                        startDate = period, nextDueDate = period, reminderEnabled = false, isAutoPostEnabled = false), "", "") }
                    val id = runBlocking { fixed.observeAllFixedExpenses().first().single { it.title == title }.id }
                    owned.getJSONArray("expenses").put(id); manifest.writeText(owned.toString())
                }
                val beta = fixed(owned, fixed).last()
                runBlocking { cases.updateFixedExpenseUseCase(beta.copy(isAutoPostEnabled = true)) }
                val request = OneTimeWorkRequestBuilder<FixedExpenseWorker>().build()
                work.enqueueUniqueWork(owned.getString("autoWork"), ExistingWorkPolicy.REPLACE, request).result.get(15, TimeUnit.SECONDS)
                await("real automatic posting") {
                    work.getWorkInfoById(request.id).get(15, TimeUnit.SECONDS)?.state == WorkInfo.State.SUCCEEDED
                }
                assertMoney(owned, tx, fixed)
                schedule(owned, manifest, fixed, cases, publisher, delay)
            } else {
                check(manifest.exists()) { "Owned fixture missing" }
                owned = JSONObject(manifest.readText())
                check(owned.getString("marker").matches(Regex("\\d+")))
                check(runBlocking { tx.getSourceById(owned.getLong("source")) }?.name == "QA-N9-${owned.getString("marker")}")
                fixed(owned, fixed) // Verify every record before mutation or cancellation.
                when (mode) {
                    "reschedule" -> schedule(owned, manifest, fixed, cases, publisher, delay)
                    "inspect" -> {
                        await("scheduled reminder workers completed") {
                            fixed(owned, fixed).all { model ->
                                val jobs = work.getWorkInfosForUniqueWork("fixed_expense_${model.id}").get(15, TimeUnit.SECONDS)
                                jobs.isNotEmpty() && jobs.all { it.state == WorkInfo.State.SUCCEEDED }
                            }
                        }
                        assertMoney(owned, tx, fixed)
                    }
                    "cleanup" -> {
                        work.cancelUniqueWork(owned.getString("autoWork")).result.get(15, TimeUnit.SECONDS)
                        fixed(owned, fixed).forEach { model ->
                            runBlocking { cases.updateFixedExpenseUseCase(model.copy(isAutoPostEnabled = false, reminderEnabled = false)) }
                            val name = "fixed_expense_${model.id}"
                            work.cancelUniqueWork(name).result.get(15, TimeUnit.SECONDS)
                            publisher.cancelNotification(name.hashCode(), NotificationManager.CHANNEL_FIXED_EXPENSE, model.id)
                        }
                        check(manifest.delete())
                    }
                    else -> error("Unknown mode $mode")
                }
            }
            val models = fixed(owned, fixed)
            val source = runBlocking { tx.getSourceById(owned.getLong("source")) }!!
            val system = targetContext.getSystemService(Context.NOTIFICATION_SERVICE) as SystemNotifications
            val channel = system.getNotificationChannel(NotificationManager.CHANNEL_FIXED_EXPENSE)
            report = JSONObject(owned.toString()).put("mode", mode).put("balance", source.balance)
                .put("reminderJobs", JSONArray(models.flatMap { model ->
                    work.getWorkInfosForUniqueWork("fixed_expense_${model.id}").get(15, TimeUnit.SECONDS).map { info ->
                        JSONObject().put("expenseId", model.id).put("id", info.id.toString())
                            .put("state", info.state.name).put("attempt", info.runAttemptCount)
                    }
                }))
                .put("channel", JSONObject().put("id", NotificationManager.CHANNEL_FIXED_EXPENSE)
                    .put("importance", channel?.importance).put("sound", channel?.sound?.toString())
                    .put("bypassDnd", channel?.canBypassDnd()))
                .put("records", JSONArray(models.map { model ->
                    JSONObject().put("id", model.id).put("title", model.title).put("due", model.nextDueDate)
                        .put("reminderAt", model.reminderTime).put("autoPost", model.isAutoPostEnabled)
                        .put("notificationId", "fixed_expense_${model.id}".hashCode())
                        .put("receiptCount", runBlocking { fixed.getFixedExpensePostings(model.id) }.size)
                }))
        } catch (error: Throwable) { failures += error.stackTraceToString() }
        finish(if (failures.isEmpty()) -1 else 0, Bundle().apply {
            putInt("failures", failures.size); putString("failure_details", failures.joinToString("\n"))
            putString("fixture", report.toString())
        })
    }

    private fun fixed(owned: JSONObject, repository: FixedExpenseRepository): List<FixedExpense> {
        val marker = owned.getString("marker")
        check(marker.matches(Regex("\\d+")))
        val ids = owned.getJSONArray("expenses")
        return (0 until ids.length()).map { index ->
            val model = runBlocking { repository.getFixedExpenseById(ids.getLong(index)) }!!
            check(model.sourceId == owned.getLong("source") && model.title.startsWith("QA-N9-$marker-"))
            model
        }
    }

    private fun assertMoney(owned: JSONObject, tx: TransactionRepository, repository: FixedExpenseRepository) {
        val models = fixed(owned, repository)
        check(models.size == 2)
        val receipts = models.map { model -> runBlocking { repository.getFixedExpensePostings(model.id) } }
        check(receipts[0].isEmpty() && receipts[1].size == 1 && !receipts[1].single().isRepeat)
        check(runBlocking { tx.getSourceById(owned.getLong("source")) }!!.balance == 9000L)
        check(runBlocking { tx.getAllTransactions() }.count { it.sourceId == owned.getLong("source") } == 1)
    }

    private fun schedule(owned: JSONObject, manifest: File, repository: FixedExpenseRepository,
        cases: FixedExpenseUseCaseGroup, publisher: NotificationManager, delay: Long) {
        val reminderAt = System.currentTimeMillis() + delay * 1000
        owned.put("reminderAt", reminderAt); manifest.writeText(owned.toString())
        fixed(owned, repository).forEach { model ->
            publisher.cancelNotification("fixed_expense_${model.id}".hashCode(), NotificationManager.CHANNEL_FIXED_EXPENSE, model.id)
            runBlocking { cases.updateFixedExpenseUseCase(model.copy(reminderEnabled = true, reminderTime = reminderAt)) }
        }
    }

    private fun await(label: String, condition: () -> Boolean) {
        val until = SystemClock.elapsedRealtime() + 60000
        while (SystemClock.elapsedRealtime() < until) { if (condition()) return; Thread.sleep(100) }
        error("Timed out: $label")
    }
}
