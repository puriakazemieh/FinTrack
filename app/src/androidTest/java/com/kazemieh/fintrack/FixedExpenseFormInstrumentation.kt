package com.kazemieh.fintrack

import android.app.Instrumentation
import android.os.Build
import android.os.Bundle
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.domain.usecase.FixedExpenseUseCaseGroup
import com.kazemieh.domain.usecase.GetDefaultCategoryUseCase
import com.kazemieh.domain.usecase.GetDefaultFinancialSourceUseCase
import com.kazemieh.fixed_expense.ui.detail.AddFixedExpenseIntent
import com.kazemieh.fixed_expense.ui.detail.AddFixedExpenseViewModel
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.getKoin

/** Emulator-only regression probe through the real form ViewModel and persistence. */
class FixedExpenseFormInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }
    override fun onStart() {
        var id: Long? = null
        var cases: FixedExpenseUseCaseGroup? = null
        val failures = mutableListOf<String>()
        try {
            check(Build.HARDWARE.contains("ranchu") || Build.HARDWARE.contains("goldfish"))
            var repository: FixedExpenseRepository? = null
            repeat(100) {
                if (repository == null) repository = runCatching { getKoin().get<FixedExpenseRepository>() }.getOrNull()
                if (repository == null) Thread.sleep(100)
            }
            val records = checkNotNull(repository)
            cases = getKoin().get<FixedExpenseUseCaseGroup>()
            val category = runBlocking { getKoin().get<GetDefaultCategoryUseCase>()(TransactionType.EXPENSE).id }!!
            val source = runBlocking { getKoin().get<GetDefaultFinancialSourceUseCase>()()?.id }!!
            val now = System.currentTimeMillis()
            val original = FixedExpense(title = "QA FORM $now", amount = 1234L, categoryId = category,
                sourceId = source, recurrence = RecurrenceType.MONTHLY, startDate = now,
                nextDueDate = now + 7200000, currencyCode = "USD", reminderEnabled = false,
                reminderTime = now + 3600000, isAutoPostEnabled = false)
            id = runBlocking { records.insertFixedExpense(original) }
            runBlocking {
                records.updateFixedExpense(original.copy(id = id!!))
                check(records.getFixedExpenseById(id!!)!!.currencyCode == "USD") { "USD precondition missing" }
                check(!records.getFixedExpenseById(id!!)!!.reminderEnabled)
            }
            lateinit var vm: AddFixedExpenseViewModel
            runOnMainSync { vm = getKoin().get<AddFixedExpenseViewModel>(); vm.onIntent(AddFixedExpenseIntent.LoadExpense(id!!)) }
            repeat(100) { if (vm.state.value.expenseId != id) Thread.sleep(100) }
            check(vm.state.value.expenseId == id) { "Form did not load" }
            runOnMainSync {
                vm.onIntent(AddFixedExpenseIntent.SetDescription("QA description-only edit"))
                vm.onIntent(AddFixedExpenseIntent.Submit)
            }
            var saved = runBlocking { records.getFixedExpenseById(id!!)!! }
            repeat(100) {
                if (saved.description != "QA description-only edit") {
                    Thread.sleep(100); saved = runBlocking { records.getFixedExpenseById(id!!)!! }
                }
            }
            check(saved.description == "QA description-only edit")
            check(saved.currencyCode == original.currencyCode) { "Currency changed ${original.currencyCode} -> ${saved.currencyCode}" }
            check(saved.reminderEnabled == original.reminderEnabled) { "Reminder toggle lost" }
            check(saved.reminderTime == original.reminderTime) { "Reminder time lost" }
            check(saved.nextDueDate == original.nextDueDate && saved.startDate == original.startDate)
        } catch (error: Throwable) { failures += error.stackTraceToString() }
        finally { id?.let { runCatching { runBlocking { cases?.deleteFixedExpenseUseCase?.invoke(it) } } } }
        finish(if (failures.isEmpty()) -1 else 0, Bundle().apply {
            putInt("tests", 1); putInt("failures", failures.size)
            putString("failure_details", failures.joinToString("\n")); putString("record_ids", id?.toString())
        })
    }
}
