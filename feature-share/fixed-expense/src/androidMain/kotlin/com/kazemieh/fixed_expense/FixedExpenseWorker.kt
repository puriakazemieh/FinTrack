package com.kazemieh.fixed_expense

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.persiandatetime.extensions.plus
import com.kazemieh.common.persiandatetime.extensions.toEpochMilliseconds
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.domain.repository.TransactionRepository
import fintrack.core.designsystem.generated.resources.*
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit
import kotlin.time.Clock

class FixedExpenseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val fixedExpenseRepository: FixedExpenseRepository by inject()
    private val transactionRepository: TransactionRepository by inject()

    override suspend fun doWork(): ListenableWorker.Result {
        val now = Clock.System.now().toEpochMilliseconds()
        val expenses = fixedExpenseRepository.observeAllFixedExpenses().first()

        expenses.filter { it.isActive && it.isAutoPostEnabled && it.nextDueDate <= now }.forEach { expense ->
            postTransaction(expense)
            if (expense.recurrence == RecurrenceType.ONCE) {
                fixedExpenseRepository.updateFixedExpense(expense.copy(isActive = false))
            } else {
                updateNextDueDate(expense)
            }
        }

        return ListenableWorker.Result.success()
    }

    private suspend fun postTransaction(expense: com.kazemieh.common.model.FixedExpense) {
        val transaction = Transaction(
            id = 0,
            amount = expense.amount.toInt(),
            categoryId = expense.categoryId,
            sourceId = expense.sourceId,
            description = expense.description ?: getString(Res.string.msg_auto_posted_fixed_expense),
            timeStamp = Clock.System.now().toEpochMilliseconds(),
            type = TransactionType.EXPENSE,
            date = ""
        )
        transactionRepository.addTransactionWithBalance(
            transaction = transaction,
            tagIds = emptyList(),
            personIds = emptyList(),
            balanceDeltas = mapOf(expense.sourceId to -expense.amount.toInt())
        )
    }

    private suspend fun updateNextDueDate(expense: com.kazemieh.common.model.FixedExpense) {
        val timeZone = TimeZone.currentSystemDefault()
        val currentDueDate = Instant.fromEpochMilliseconds(expense.nextDueDate).toPersianDateTime(timeZone)

        val next = when (expense.recurrence) {
            RecurrenceType.DAILY -> currentDueDate.plus(1, DateTimeUnit.DAY)
            RecurrenceType.WEEKLY -> currentDueDate.plus(7, DateTimeUnit.DAY)
            RecurrenceType.MONTHLY -> currentDueDate.plus(1, DateTimeUnit.MONTH)
            RecurrenceType.YEARLY -> currentDueDate.plus(1, DateTimeUnit.YEAR)
            else -> currentDueDate // Handle CUSTOM/ONCE appropriately if needed
        }

        fixedExpenseRepository.updateNextDueDate(expense.id, next.toEpochMilliseconds(timeZone))
    }

    companion object {
        fun enqueuePeriodicWork(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<FixedExpenseWorker>(1, TimeUnit.DAYS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "FixedExpenseWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
