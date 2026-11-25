package com.kazemieh.fintrack

import android.app.Instrumentation
import android.os.Build
import android.os.Bundle
import com.kazemieh.common.model.*
import com.kazemieh.domain.repository.BackupRepository
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.usecase.GetDefaultCategoryUseCase
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.getKoin

/** Host must snapshot and restore the emulator DB/prefs; all money uses an owned QA source. */
class FixedExpenseAtomicInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }
    override fun onStart() {
        var sourceId = 0L
        val expenses = mutableListOf<Long>()
        val transactions = mutableListOf<Long>()
        val failures = mutableListOf<String>()
        var tests = 0
        try {
            check(Build.HARDWARE.contains("ranchu") || Build.HARDWARE.contains("goldfish")) { "Emulator only" }
            var repository: TransactionRepository? = null
            repeat(100) {
                if (repository == null) repository = runCatching { getKoin().get<TransactionRepository>() }.getOrNull()
                if (repository == null) Thread.sleep(100)
            }
            val tx = checkNotNull(repository)
            val fixed = getKoin().get<FixedExpenseRepository>()
            val backup = getKoin().get<BackupRepository>()
            val category = runBlocking { getKoin().get<GetDefaultCategoryUseCase>()(TransactionType.EXPENSE).id }!!
            val marker = "QA-FIXED-ATOMIC-${System.currentTimeMillis()}"
            sourceId = runBlocking { tx.addSource(Source(name = marker, balance = 10000, colorId = 0, iconId = 0)) }
            check(sourceId > 4)
            val originalCount = runBlocking { tx.getTransactionCount() }
            val due = System.currentTimeMillis() + 3600000
            fun expense(id: Long) = runBlocking { fixed.getFixedExpenseById(id) }!!
            fun insert(recurrence: RecurrenceType): Long = runBlocking {
                fixed.insertFixedExpense(FixedExpense(title = "$marker-$recurrence", amount = 1000,
                    categoryId = category, sourceId = sourceId, recurrence = recurrence,
                    startDate = due, nextDueDate = due, reminderEnabled = false, isAutoPostEnabled = false))
            }.also { expenses += it }
            fun post(request: FixedExpensePostingRequest): Long = runBlocking {
                tx.addFixedExpenseTransactionWithBalance(request, Transaction(id = 0, amount = request.expense.amount,
                    currencyCode = request.expense.currencyCode, categoryId = category, sourceId = sourceId,
                    description = request.expense.title, type = TransactionType.EXPENSE))
            }
            val monthly = insert(RecurrenceType.MONTHLY)
            val ordinary = FixedExpensePostingRequest("$marker-monthly", expense(monthly), due, due + 86400000, true)
            val first = post(ordinary).also { transactions += it }
            check(first > 0 && post(ordinary) == 0L)
            check(expense(monthly).nextDueDate == due + 86400000)
            tests++
            val afterFirst = expense(monthly)
            val repeat = ordinary.copy(operationId = "$marker-monthly-repeat", expense = afterFirst, isRepeat = true)
            val second = post(repeat).also { transactions += it }
            check(second > 0 && post(repeat) == 0L && expense(monthly) == afterFirst)
            tests++
            val once = insert(RecurrenceType.ONCE)
            val oneTime = FixedExpensePostingRequest("$marker-once", expense(once), due, due, false)
            val third = post(oneTime).also { transactions += it }
            check(third > 0 && post(oneTime) == 0L && !expense(once).isActive)
            tests++
            val afterOnce = expense(once)
            val onceRepeat = oneTime.copy(operationId = "$marker-once-repeat", expense = afterOnce, isRepeat = true)
            val fourth = post(onceRepeat).also { transactions += it }
            check(fourth > 0 && post(onceRepeat) == 0L && expense(once) == afterOnce)
            tests++
            check(post(repeat.copy(operationId = "$marker-stale", expense = afterFirst.copy(amount = 2000))) == 0L)
            check(runBlocking { tx.getSourceById(sourceId) }!!.balance == 6000L)
            check(runBlocking { tx.getTransactionCount() } == originalCount + 4)
            val receipts = runBlocking { backup.getBackupData() }.fixedExpensePostings.filter { it.expenseId in expenses }
            check(receipts.size == 4 && receipts.count { it.isRepeat } == 2 && receipts.all { it.periodDueDate == due })
            check(receipts.map { it.transactionId }.toSet() == transactions.toSet())
            tests++
        } catch (error: Throwable) { failures += error.stackTraceToString() }
        finish(if (failures.isEmpty() && tests == 5) -1 else 0, Bundle().apply {
            putInt("tests", tests); putInt("failures", failures.size); putString("failure_details", failures.joinToString("\n"))
            putLong("source_id", sourceId); putString("expense_ids", expenses.joinToString(","))
            putString("transaction_ids", transactions.joinToString(","))
        })
    }
}
