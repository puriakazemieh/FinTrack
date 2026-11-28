package com.kazemieh.fintrack

import android.app.Instrumentation
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.*
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.usecase.GetDefaultCategoryUseCase
import com.kazemieh.fixed_expense.ui.list.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.getKoin

/** Host snapshots/restores all business data. Only this probe's synthetic source is debited. */
class FixedExpensePostingUiInstrumentation : Instrumentation() {
    private var prepareOnly = false
    override fun onCreate(arguments: Bundle?) {
        prepareOnly = arguments?.getString("mode") == "prepare"
        super.onCreate(arguments); start()
    }
    override fun onStart() {
        var tests = 0
        val failures = mutableListOf<String>()
        val sourceIds = mutableListOf<Long>()
        val expenseIds = mutableListOf<Long>()
        var transactionIds = emptyList<Long>()
        val vms = mutableListOf<FixedExpenseListViewModel>()
        var trigger: String? = null
        fun await(label: String, condition: () -> Boolean) {
            repeat(200) { if (condition()) return; Thread.sleep(50) }
            check(condition()) { "Timeout: $label" }
        }
        fun test(name: String, block: () -> Unit) {
            tests++
            try { block() } catch (error: Throwable) { failures += "$name: ${error.stackTraceToString()}" }
        }
        fun sql(statement: String) {
            SQLiteDatabase.openDatabase(targetContext.getDatabasePath("fintrack.db").path, null,
                SQLiteDatabase.OPEN_READWRITE).use { it.execSQL(statement) }
        }
        try {
            check(Build.HARDWARE.contains("ranchu") || Build.HARDWARE.contains("goldfish")) { "Emulator only" }
            var repository: TransactionRepository? = null
            await("Koin") { repository = runCatching { getKoin().get<TransactionRepository>() }.getOrNull(); repository != null }
            val tx = checkNotNull(repository)
            val fixed = getKoin().get<FixedExpenseRepository>()
            val category = runBlocking { getKoin().get<GetDefaultCategoryUseCase>()(TransactionType.EXPENSE).id }!!
            val source = runBlocking { tx.addSource(Source(name = "QA Posting UI", balance = 10000, colorId = 0, iconId = 0)) }
            sourceIds += source
            val now = System.currentTimeMillis()
            fun create(title: String, recurrence: RecurrenceType): FixedExpense {
                val id = runBlocking { fixed.insertFixedExpense(FixedExpense(title = title, amount = 1000,
                    categoryId = category, sourceId = source, recurrence = recurrence, startDate = now - 60000,
                    nextDueDate = now - 60000, reminderEnabled = false, isAutoPostEnabled = false)) }
                expenseIds += id
                return runBlocking { fixed.getFixedExpenseById(id) }!!
            }
            val monthly = create("QA ثبت دورهٔ هزینهٔ خدمات ساختمان با عنوان کامل و مبلغ مستقل", RecurrenceType.MONTHLY)
            val once = create("QA یک‌باره", RecurrenceType.ONCE)
            val stale = create("QA خطای ثبت", RecurrenceType.MONTHLY)
            fun entries() = runBlocking { tx.getAllTransactions() }.filter { it.sourceId == source }
            fun receiptCount(id: Long) = runBlocking { fixed.getFixedExpensePostings(id) }.size
            fun expense(id: Long) = runBlocking { fixed.getFixedExpenseById(id) }!!
            if (!prepareOnly) {
                lateinit var left: FixedExpenseListViewModel
                lateinit var right: FixedExpenseListViewModel
                runOnMainSync {
                    left = getKoin().get(); right = getKoin().get(); vms += left; vms += right
                }
                fun ready(vm: FixedExpenseListViewModel, id: Long) = vm.state.value.postingStatuses.containsKey(id) &&
                    vm.state.value.expenses.any { it.id == id } && vm.state.value.postingIds.isEmpty()
                await("two status consumers") { ready(left, monthly.id) && ready(right, monthly.id) }
                test("ordinary-double-tap-and-two-surface-race") {
                    runOnMainSync {
                        left.onIntent(FixedExpenseListIntent.RegisterAsTransaction(monthly))
                        left.onIntent(FixedExpenseListIntent.RegisterAsTransaction(monthly))
                        right.onIntent(FixedExpenseListIntent.RegisterAsTransaction(monthly))
                    }
                    await("one primary, both idle") { receiptCount(monthly.id) == 1 &&
                        ready(left, monthly.id) && ready(right, monthly.id) }
                    check(entries().size == 1)
                    check(expense(monthly.id).nextDueDate > monthly.nextDueDate)
                }
                test("both-statuses-update-without-navigation") {
                    await("both counts") { left.state.value.postingStatuses[monthly.id]?.postingCount == 1 &&
                        right.state.value.postingStatuses[monthly.id]?.postingCount == 1 }
                    check(left.state.value.postingStatuses[monthly.id]?.periodDueDate == monthly.nextDueDate)
                    check(right.state.value.postingStatuses[monthly.id] == left.state.value.postingStatuses[monthly.id])
                }
                test("second-click-only-opens-confirmation") {
                    runOnMainSync { left.onIntent(FixedExpenseListIntent.RegisterAsTransaction(expense(monthly.id))) }
                    val command = checkNotNull(left.state.value.repeatConfirmation)
                    check(command.periodDueDate == monthly.nextDueDate && !command.repeatOperationId.isNullOrBlank())
                    check(entries().size == 1 && receiptCount(monthly.id) == 1)
                }
                test("dismiss-does-not-write") {
                    val before = expense(monthly.id)
                    runOnMainSync { left.onIntent(FixedExpenseListIntent.DismissRepeat) }
                    check(left.state.value.repeatConfirmation == null)
                    check(expense(monthly.id) == before && entries().size == 1)
                }
                test("confirmed-double-tap-adds-one-without-advancing") {
                    val before = expense(monthly.id)
                    runOnMainSync {
                        left.onIntent(FixedExpenseListIntent.RegisterAsTransaction(before))
                        left.onIntent(FixedExpenseListIntent.ConfirmRepeat)
                        left.onIntent(FixedExpenseListIntent.ConfirmRepeat)
                    }
                    await("confirmed once") { receiptCount(monthly.id) == 2 && ready(left, monthly.id) }
                    check(expense(monthly.id).nextDueDate == before.nextDueDate && entries().size == 2)
                    check(runBlocking { fixed.getFixedExpensePostings(monthly.id) }.count { it.isRepeat } == 1)
                }
                test("failed-repeat-keeps-nonce-and-retry-does-not-double-advance") {
                    val before = expense(monthly.id)
                    runOnMainSync { left.onIntent(FixedExpenseListIntent.RegisterAsTransaction(before)) }
                    val command = checkNotNull(left.state.value.repeatConfirmation)
                    trigger = "qa_fixed_ui_fail_${monthly.id}"
                    sql("CREATE TRIGGER $trigger BEFORE INSERT ON fixed_expense_posting WHEN NEW.expenseId = ${monthly.id} AND NEW.isRepeat = 1 BEGIN SELECT RAISE(ABORT, 'QA receipt failure'); END")
                    runOnMainSync { left.onIntent(FixedExpenseListIntent.ConfirmRepeat) }
                    await("error state") { monthly.id in left.state.value.postingFailures && ready(left, monthly.id) }
                    check(left.state.value.repeatConfirmation == command)
                    check(receiptCount(monthly.id) == 2 && entries().size == 2)
                    check(expense(monthly.id) == before)
                    sql("DROP TRIGGER $trigger"); trigger = null
                    runOnMainSync { left.onIntent(FixedExpenseListIntent.ConfirmRepeat) }
                    await("retry committed once") { receiptCount(monthly.id) == 3 && ready(left, monthly.id) }
                    check(runBlocking { fixed.getFixedExpensePostings(monthly.id) }.count { it.operationId == command.repeatOperationId } == 1)
                    check(expense(monthly.id).nextDueDate == before.nextDueDate && entries().size == 3)
                }
                test("one-time-primary-closes-and-second-click-needs-confirmation") {
                    await("one-time ready") { ready(left, once.id) }
                    runOnMainSync { left.onIntent(FixedExpenseListIntent.RegisterAsTransaction(once)) }
                    await("one-time primary") { receiptCount(once.id) == 1 && ready(left, once.id) }
                    check(!expense(once.id).isActive)
                    await("one-time status") { left.state.value.postingStatuses[once.id]?.hasPrimaryPosting == true }
                    runOnMainSync { left.onIntent(FixedExpenseListIntent.RegisterAsTransaction(expense(once.id))) }
                    check(left.state.value.repeatConfirmation?.expense?.id == once.id && entries().size == 4)
                }
                test("confirmed-one-time-repeat-keeps-closed-state") {
                    runOnMainSync { left.onIntent(FixedExpenseListIntent.ConfirmRepeat) }
                    await("one-time repeat") { receiptCount(once.id) == 2 && ready(left, once.id) }
                    check(!expense(once.id).isActive && expense(once.id).nextDueDate == once.nextDueDate)
                    check(entries().size == 5)
                }
                test("zero-result-is-failure-not-success") {
                    await("unposted fixture ready") { ready(left, stale.id) }
                    runOnMainSync { left.onIntent(FixedExpenseListIntent.RegisterAsTransaction(stale.copy(amount = 9999))) }
                    await("zero feedback") { stale.id in left.state.value.postingFailures && ready(left, stale.id) }
                    check(receiptCount(stale.id) == 0 && entries().size == 5)
                    check(left.state.value.postingMessages[stale.id] != null)
                    check(expense(stale.id) == stale)
                    check(runBlocking { tx.getSourceById(source) }!!.balance == 5000L)
                }
            }
            transactionIds = entries().map { it.id }
        } catch (error: Throwable) { failures += error.stackTraceToString() }
        finally {
            trigger?.let { runCatching { sql("DROP TRIGGER IF EXISTS $it") } }
            runOnMainSync { vms.forEach { it.viewModelScope.cancel() } }
        }
        finish(if (failures.isEmpty()) -1 else 0, Bundle().apply {
            putInt("tests", tests); putInt("failures", failures.size)
            putString("failure_details", failures.joinToString("\n"))
            putString("source_ids", sourceIds.joinToString(",")); putString("expense_ids", expenseIds.joinToString(","))
            putString("transaction_ids", transactionIds.joinToString(","))
        })
    }
}
