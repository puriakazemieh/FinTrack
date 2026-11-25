package com.kazemieh.fintrack

import android.app.Instrumentation
import android.os.Build
import android.os.Bundle
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.domain.usecase.FixedExpenseUseCaseGroup
import com.kazemieh.domain.usecase.GetDefaultCategoryUseCase
import com.kazemieh.domain.usecase.GetDefaultFinancialSourceUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.getKoin

/** Opt-in emulator integration probe; exercises real repositories, DI and WorkManager. */
class FixedExpenseSchedulingInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }

    override fun onStart() {
        val failures = mutableListOf<String>()
        val ids = mutableListOf<Long>()
        var cases: FixedExpenseUseCaseGroup? = null
        var tests = 0
        var step = "setup"
        var firstDelay = -1L
        var replacementDelay = -1L
        val marker = "QA-FIXED-SCHED-${System.currentTimeMillis()}"
        try {
            check(Build.HARDWARE.contains("ranchu") || Build.HARDWARE.contains("goldfish")) { "Emulator only" }
            var repository: FixedExpenseRepository? = null
            repeat(100) {
                if (repository == null) repository = runCatching { getKoin().get<FixedExpenseRepository>() }.getOrNull()
                if (repository == null) Thread.sleep(100)
            }
            val records = checkNotNull(repository) { "DI unavailable" }
            val useCases = getKoin().get<FixedExpenseUseCaseGroup>().also { cases = it }
            val categoryId = runBlocking { getKoin().get<GetDefaultCategoryUseCase>()(TransactionType.EXPENSE).id } ?: error("Category missing")
            val sourceId = runBlocking { getKoin().get<GetDefaultFinancialSourceUseCase>()()?.id } ?: error("Source missing")
            val work = WorkManager.getInstance(targetContext)
            fun infos(id: Long) = work.getWorkInfosForUniqueWork("fixed_expense_$id").get()
            fun enqueued(id: Long, previousId: java.util.UUID? = null): WorkInfo {
                repeat(100) {
                    infos(id).singleOrNull { it.state == WorkInfo.State.ENQUEUED && it.id != previousId }?.let { return it }
                    Thread.sleep(100)
                }
                error("No enqueued work for $id: ${infos(id).map { it.state }}")
            }
            fun cancelled(id: Long) {
                repeat(100) {
                    if (infos(id).none { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.BLOCKED }) return
                    Thread.sleep(100)
                }
                error("Work remains active for $id")
            }
            val now = System.currentTimeMillis()
            val firstTime = now + 3_600_000L
            val replacementTime = now + 10_800_000L
            val initial = FixedExpense(
                title = "$marker-first", amount = 1000L, categoryId = categoryId, sourceId = sourceId,
                recurrence = RecurrenceType.MONTHLY, startDate = now, nextDueDate = now + 7_200_000L,
                reminderEnabled = true, reminderTime = firstTime, isAutoPostEnabled = false
            )
            step = "create uses explicit reminder time"; tests++
            runBlocking { useCases.addFixedExpenseUseCase(initial, "QA reminder", initial.title) }
            var saved = runBlocking { records.observeAllFixedExpenses().first().single { it.title == initial.title } }
            val id = saved.id.also { ids += it }
            val first = enqueued(id)
            firstDelay = first.initialDelayMillis
            check(firstDelay in 3_540_000L..3_600_000L) { "Used accounting due date instead of reminder: $firstDelay" }

            step = "edit replaces the old request"; tests++
            runBlocking { useCases.updateFixedExpenseUseCase(saved.copy(reminderTime = replacementTime)) }
            val replacement = enqueued(id, first.id)
            replacementDelay = replacement.initialDelayMillis
            check(replacement.id != first.id)
            check(replacementDelay in 10_680_000L..10_800_000L)
            saved = runBlocking { records.getFixedExpenseById(id)!! }

            step = "turning reminder off cancels work"; tests++
            runBlocking { useCases.updateFixedExpenseUseCase(saved.copy(reminderEnabled = false)) }
            cancelled(id)
            saved = runBlocking { records.getFixedExpenseById(id)!! }
            check(!saved.reminderEnabled && saved.reminderTime == replacementTime)

            step = "turning reminder back on schedules work"; tests++
            runBlocking { useCases.updateFixedExpenseUseCase(saved.copy(reminderEnabled = true)) }
            check(enqueued(id, replacement.id).id != replacement.id)
            saved = runBlocking { records.getFixedExpenseById(id)!! }

            step = "inactive expense cancels work"; tests++
            runBlocking { useCases.updateFixedExpenseUseCase(saved.copy(isActive = false)) }
            cancelled(id)
            saved = runBlocking { records.getFixedExpenseById(id)!! }
            check(!saved.isActive)

            step = "moving reminder to the past leaves no old future request"; tests++
            runBlocking { useCases.updateFixedExpenseUseCase(saved.copy(isActive = true, reminderTime = replacementTime)) }
            enqueued(id)
            saved = runBlocking { records.getFixedExpenseById(id)!! }
            runBlocking { useCases.updateFixedExpenseUseCase(saved.copy(reminderTime = now - 60_000L)) }
            cancelled(id)

            step = "new disabled reminder saves without work"; tests++
            val disabled = initial.copy(title = "$marker-disabled", reminderEnabled = false)
            runBlocking { useCases.addFixedExpenseUseCase(disabled, "QA reminder", disabled.title) }
            val second = runBlocking { records.observeAllFixedExpenses().first().single { it.title == disabled.title } }
            ids += second.id
            check(!second.reminderEnabled)
            cancelled(second.id)

            step = "delete cancels an active request"; tests++
            saved = runBlocking { records.getFixedExpenseById(id)!! }
            runBlocking { useCases.updateFixedExpenseUseCase(saved.copy(isActive = true, reminderEnabled = true, reminderTime = firstTime)) }
            enqueued(id)
            runBlocking { useCases.deleteFixedExpenseUseCase(id) }
            cancelled(id)
            check(runBlocking { records.getFixedExpenseById(id) } == null)
        } catch (error: Throwable) {
            failures += "$step: ${error.stackTraceToString()}"
        } finally {
            ids.forEach { id -> runCatching { runBlocking { cases?.deleteFixedExpenseUseCase?.invoke(id) } } }
        }
        finish(if (failures.isEmpty() && tests == 8) -1 else 0, Bundle().apply {
            putInt("tests", tests); putInt("failures", failures.size)
            putString("failure_details", failures.joinToString("\n"))
            putString("record_ids", ids.joinToString(",")); putString("marker", marker)
            putLong("first_delay", firstDelay); putLong("replacement_delay", replacementDelay)
        })
    }
}
