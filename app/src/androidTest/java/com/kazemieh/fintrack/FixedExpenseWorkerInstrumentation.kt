package com.kazemieh.fintrack

import android.app.Instrumentation
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.work.*
import com.kazemieh.common.model.*
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.usecase.GetDefaultCategoryUseCase
import com.kazemieh.fixed_expense.FixedExpenseWorker
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.koin.java.KoinJavaComponent.getKoin

/** Real concurrent/retrying WorkManager runs; the guarded host restores all fixture data. */
class FixedExpenseWorkerInstrumentation : Instrumentation() {
    private var arguments = Bundle()
    override fun onCreate(arguments: Bundle?) { this.arguments = arguments ?: Bundle(); super.onCreate(arguments); start() }
    override fun onStart() {
        val failures = mutableListOf<String>()
        var tests = 0
        var source = 0L
        val expenses = mutableListOf<Long>()
        val transactions = mutableListOf<Long>()
        var retryAttempt = 0
        val owned = File(targetContext.filesDir, "qa-fixed-worker-owned.json")
        try {
            check(Build.HARDWARE.contains("ranchu") || Build.HARDWARE.contains("goldfish")) { "Emulator only" }
            var repository: TransactionRepository? = null
            repeat(100) {
                if (repository == null) repository = runCatching { getKoin().get<TransactionRepository>() }.getOrNull()
                if (repository == null) Thread.sleep(100)
            }
            val tx = checkNotNull(repository)
            val fixed = getKoin().get<FixedExpenseRepository>()
            val work = WorkManager.getInstance(targetContext)
            if (arguments.getString("mode") == "cleanup") {
                cleanup(owned, work, fixed)
            } else {
                check(!owned.exists()) { "Unfinished worker fixture requires recovery before a fresh snapshot" }
                val marker = System.currentTimeMillis().toString()
                val trigger = "qa_fixed_abort_$marker"
                val names = listOf("QA_FIXED_WORKER_${marker}_A", "QA_FIXED_WORKER_${marker}_B", "QA_FIXED_WORKER_${marker}_RETRY")
                val category = runBlocking { getKoin().get<GetDefaultCategoryUseCase>()(TransactionType.EXPENSE).id }!!
                val oldIds = runBlocking { tx.getAllTransactions() }.map { it.id }.toSet()
                source = runBlocking { tx.addSource(Source(name = "QA-FIXED-WORKER-$marker", balance = 10000, colorId = 0, iconId = 0)) }
                val due = System.currentTimeMillis() - 2 * 86400000L - 60000
                fun insert(title: String, recurrence: RecurrenceType, auto: Boolean) = runBlocking {
                    fixed.insertFixedExpense(FixedExpense(title = title, amount = 1000, categoryId = category, sourceId = source,
                        recurrence = recurrence, startDate = due, nextDueDate = due, reminderEnabled = false, isAutoPostEnabled = auto))
                }.also { expenses += it }
                // Both are disabled until the recovery manifest has been written.
                val daily = insert("QA-FIXED-WORKER-$marker-daily", RecurrenceType.DAILY, false)
                val once = insert("QA-FIXED-WORKER-$marker-once", RecurrenceType.ONCE, false)
                owned.writeText(JSONObject().put("marker", marker).put("source", source).put("trigger", trigger)
                    .put("names", org.json.JSONArray(names)).put("expenses", org.json.JSONArray(expenses)).toString())
                fun current(id: Long) = runBlocking { fixed.getFixedExpenseById(id) }!!
                fun enqueue(name: String): OneTimeWorkRequest {
                    val request = OneTimeWorkRequestBuilder<FixedExpenseWorker>()
                        .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS).build()
                    work.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request).result.get(15, TimeUnit.SECONDS)
                    return request
                }
                fun info(request: OneTimeWorkRequest) = work.getWorkInfoById(request.id).get(15, TimeUnit.SECONDS)!!
                runBlocking { fixed.updateFixedExpense(current(daily).copy(isAutoPostEnabled = true)) }
                val a = enqueue(names[0]); val b = enqueue(names[1])
                await("concurrent catch-up") { info(a).state == WorkInfo.State.SUCCEEDED && info(b).state == WorkInfo.State.SUCCEEDED }
                check(runBlocking { tx.getTransactionCount() } == oldIds.size + 3L)
                check(runBlocking { tx.getSourceById(source) }!!.balance == 7000L)
                check(current(daily).nextDueDate == due + 3 * 86400000L)
                check(runBlocking { fixed.getFixedExpensePostings(daily) }.map { it.periodDueDate }.toSet() ==
                    setOf(due, due + 86400000L, due + 2 * 86400000L))
                tests++
                val replay = enqueue(names[0])
                await("worker replay") { info(replay).state == WorkInfo.State.SUCCEEDED }
                check(runBlocking { tx.getTransactionCount() } == oldIds.size + 3L)
                tests++
                sql("CREATE TRIGGER $trigger BEFORE INSERT ON fixed_expense_posting WHEN NEW.expenseId=$once " +
                    "BEGIN SELECT RAISE(ABORT, 'QA fixed worker last-write failure'); END")
                runBlocking { fixed.updateFixedExpense(current(once).copy(isAutoPostEnabled = true)) }
                val retry = enqueue(names[2])
                await("retry after rolled-back failure") { info(retry).runAttemptCount >= 1 && info(retry).state == WorkInfo.State.ENQUEUED }
                retryAttempt = info(retry).runAttemptCount
                check(runBlocking { tx.getTransactionCount() } == oldIds.size + 3L)
                check(runBlocking { tx.getSourceById(source) }!!.balance == 7000L)
                check(current(once).isActive && current(once).nextDueDate == due)
                check(runBlocking { fixed.getFixedExpensePostings(once) }.isEmpty())
                tests++
                sql("DROP TRIGGER $trigger")
                await("retry recovery") { info(retry).state == WorkInfo.State.SUCCEEDED }
                check(runBlocking { tx.getTransactionCount() } == oldIds.size + 4L)
                check(!current(once).isActive && runBlocking { fixed.getFixedExpensePostings(once) }.size == 1)
                check(runBlocking { tx.getSourceById(source) }!!.balance == 6000L)
                tests++
                runBlocking { fixed.updateFixedExpense(current(daily).copy(isAutoPostEnabled = false, nextDueDate = due)) }
                val disabled = enqueue(names[0])
                await("disabled automatic posting") { info(disabled).state == WorkInfo.State.SUCCEEDED }
                check(runBlocking { tx.getTransactionCount() } == oldIds.size + 4L && current(daily).nextDueDate == due)
                tests++
                transactions += runBlocking { tx.getAllTransactions() }.filter { it.id !in oldIds }.map { it.id }
            }
        } catch (error: Throwable) { failures += error.stackTraceToString() }
        finally {
            runCatching { cleanup(owned, WorkManager.getInstance(targetContext), getKoin().get()) }
                .onFailure { failures += "cleanup: ${it.stackTraceToString()}" }
        }
        finish(if (failures.isEmpty() && (tests == 5 || arguments.getString("mode") == "cleanup")) -1 else 0, Bundle().apply {
            putInt("tests", tests); putInt("failures", failures.size); putString("failure_details", failures.joinToString("\n"))
            putLong("source_id", source); putString("expense_ids", expenses.joinToString(","))
            putString("transaction_ids", transactions.joinToString(",")); putInt("retry_attempt", retryAttempt)
        })
    }
    private fun sql(statement: String) {
        SQLiteDatabase.openDatabase(targetContext.getDatabasePath("fintrack.db").absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
            .use { it.execSQL(statement) }
    }
    private fun cleanup(file: File, work: WorkManager, fixed: FixedExpenseRepository) {
        if (!file.exists()) return
        val data = JSONObject(file.readText())
        val trigger = data.getString("trigger")
        check(trigger.matches(Regex("qa_fixed_abort_\\d+")))
        val names = data.getJSONArray("names")
        for (i in 0 until names.length()) {
            val name = names.getString(i); check(name.startsWith("QA_FIXED_WORKER_"))
            work.cancelUniqueWork(name).result.get(15, TimeUnit.SECONDS)
        }
        sql("DROP TRIGGER IF EXISTS $trigger")
        val expenses = data.getJSONArray("expenses")
        for (i in 0 until expenses.length()) runBlocking {
            val id = expenses.getLong(i); check(id > 4)
            fixed.getFixedExpenseById(id)?.let {
                check(it.title.startsWith("QA-FIXED-WORKER-${data.getString("marker")}-"))
                fixed.updateFixedExpense(it.copy(isAutoPostEnabled = false))
            }
        }
        check(file.delete())
    }
    private fun await(name: String, condition: () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + 60000
        while (SystemClock.elapsedRealtime() < deadline) { if (condition()) return; Thread.sleep(200) }
        error("Timed out: $name")
    }
}
