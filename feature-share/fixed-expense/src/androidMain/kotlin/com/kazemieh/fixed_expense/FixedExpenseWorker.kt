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
import com.kazemieh.domain.usecase.FixedExpenseUseCaseGroup
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

    private val fixedExpenseUseCases: FixedExpenseUseCaseGroup by inject()

    override suspend fun doWork(): ListenableWorker.Result {
        val now = Clock.System.now().toEpochMilliseconds()
        val expenses = fixedExpenseUseCases.observeAllFixedExpensesUseCase().first()

        expenses.filter { it.isActive && it.isAutoPostEnabled && it.recurrence != RecurrenceType.NONE }.forEach { expense ->
            var currentExpense = expense
            // Catch up all missed periods until nextDueDate is in the future.
            while (currentExpense.isActive && currentExpense.nextDueDate <= now) {
                fixedExpenseUseCases.postFixedExpenseAsTransactionUseCase(currentExpense)
                // Reload or manually advance to continue loop if needed.
                // Since postFixedExpenseAsTransactionUseCase updates the DB, 
                // we should either reload from repo or use the advance logic here.
                
                // Better approach: use a helper that returns the updated expense.
                // Or just reload from repository since it's a small list.
                val updated = fixedExpenseUseCases.getFixedExpenseByIdUseCase(currentExpense.id)
                if (updated == null || !updated.isActive || updated.nextDueDate <= currentExpense.nextDueDate) {
                    break // Prevent infinite loop if something went wrong or it became inactive
                }
                currentExpense = updated
            }
        }

        return ListenableWorker.Result.success()
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
