package com.kazemieh.fintrack

import android.app.Instrumentation
import android.os.Build
import android.os.Bundle
import com.kazemieh.common.model.*
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.usecase.FixedExpenseUseCaseGroup
import com.kazemieh.domain.usecase.GetDefaultCategoryUseCase
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.getKoin

/** Real domain regressions; host owns full DB/prefs restoration and synthetic sources. */
class FixedExpenseDomainInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }
    override fun onStart() {
        val sources = mutableListOf<Long>()
        val expenses = mutableListOf<Long>()
        val transactionIds = mutableListOf<Long>()
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
            val cases = getKoin().get<FixedExpenseUseCaseGroup>()
            val category = runBlocking { getKoin().get<GetDefaultCategoryUseCase>()(TransactionType.EXPENSE).id }!!
            val oldIds = runBlocking { tx.getAllTransactions() }.map { it.id }.toSet()
            fun create(name: String, once: Boolean = false, usd: Boolean = false): FixedExpense {
                val source = runBlocking { tx.addSource(Source(name = "QA-DOMAIN-$name-${System.currentTimeMillis()}",
                    balance = 10000, colorId = 0, iconId = 0)) }
                sources += source
                if (usd) runBlocking { tx.updateSource(tx.getSourceById(source)!!.copy(currencyCode = "USD")) }
                val due = System.currentTimeMillis() - 60000
                val id = runBlocking { fixed.insertFixedExpense(FixedExpense(title = "QA-DOMAIN-$name", amount = 1000,
                    categoryId = category, sourceId = source, recurrence = if (once) RecurrenceType.ONCE else RecurrenceType.MONTHLY,
                    startDate = due, nextDueDate = due, reminderEnabled = false, isAutoPostEnabled = false)) }
                expenses += id
                if (usd) runBlocking { fixed.updateFixedExpense(fixed.getFixedExpenseById(id)!!.copy(currencyCode = "USD")) }
                return runBlocking { fixed.getFixedExpenseById(id) }!!
            }
            fun post(expense: FixedExpense) = runBlocking { cases.postFixedExpenseAsTransactionUseCase(expense) }
            fun entries(expense: FixedExpense) = runBlocking { tx.getAllTransactions() }.filter { it.sourceId == expense.sourceId }
            fun test(name: String, block: () -> Unit) {
                tests++
                try { block() } catch (error: Throwable) { failures += "$name: ${error.stackTraceToString()}" }
            }
            test("same-snapshot") {
                val expense = create("same-snapshot")
                post(expense); post(expense)
                check(entries(expense).size == 1) { "Stale snapshot posted ${entries(expense).size} times" }
            }
            test("reloaded-current-period") {
                val expense = create("reloaded-current-period")
                post(expense)
                post(runBlocking { fixed.getFixedExpenseById(expense.id) }!!)
                check(entries(expense).size == 1) { "Reloading UI posted the next period without confirmation" }
            }
            test("stored-usd") {
                val expense = create("stored-usd", usd = true)
                check(expense.currencyCode == "USD" && runBlocking { tx.getSourceById(expense.sourceId) }!!.currencyCode == "USD")
                post(expense)
                check(entries(expense).single().currencyCode == "USD") { "Stored USD was silently relabelled" }
            }
            test("stale-one-time") {
                val expense = create("stale-one-time", once = true)
                post(expense); post(expense)
                check(entries(expense).size == 1) { "Stale one-time expense posted twice" }
                check(!runBlocking { fixed.getFixedExpenseById(expense.id) }!!.isActive)
            }
            test("deleted-snapshot") {
                val expense = create("deleted-snapshot")
                runBlocking { fixed.deleteFixedExpense(expense.id) }
                post(expense)
                check(entries(expense).isEmpty() && runBlocking { fixed.getFixedExpenseById(expense.id) } == null) {
                    "Deleted expense posted or resurrected"
                }
            }
            transactionIds += runBlocking { tx.getAllTransactions() }.filter { it.id !in oldIds }.map { it.id }
        } catch (error: Throwable) { failures += "setup: ${error.stackTraceToString()}" }
        finish(if (failures.isEmpty() && tests == 5) -1 else 0, Bundle().apply {
            putInt("tests", tests); putInt("failures", failures.size); putString("failure_details", failures.joinToString("\n"))
            putString("source_ids", sources.joinToString(",")); putString("expense_ids", expenses.joinToString(","))
            putString("transaction_ids", transactionIds.joinToString(","))
        })
    }
}
