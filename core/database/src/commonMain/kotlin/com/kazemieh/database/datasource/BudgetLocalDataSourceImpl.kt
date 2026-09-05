package com.kazemieh.database.datasource

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kazemieh.common.model.Budget
import com.kazemieh.common.model.BudgetPeriod
import com.kazemieh.common.model.BudgetWithProgress
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.SyncStatus
import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.persiandatetime.extensions.toEpochMilliseconds
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import com.kazemieh.common.persiandatetime.extensions.plus
import com.kazemieh.common.persiandatetime.extensions.minus
import com.kazemieh.common.persiandatetime.extensions.dayOfWeekIndex
import com.kazemieh.data_contract.datasource.BudgetLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.ObserveBudgetsByRange
import com.kazemieh.database.ObserveBudgets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

class BudgetLocalDataSourceImpl(
    private val db: FinTrackDatabase
) : BudgetLocalDataSource {

    private val queries = db.budgetQueries
    private val transactionQueries = db.transactionQueries

    override fun observeBudgetsWithProgress(from: Long, to: Long): Flow<List<BudgetWithProgress>> {
        val budgetsFlow: Flow<List<ObserveBudgetsByRange>> = queries.observeBudgetsByRange(fromAt = from, toAt = to).asFlow().mapToList(Dispatchers.Default)
        val transactionsFlow: Flow<List<com.kazemieh.database.Transactions>> = transactionQueries.getAllTransactions().asFlow().mapToList(Dispatchers.Default)
        
        return budgetsFlow.combine(transactionsFlow) { budgets, _ ->
            budgets.map { item ->
                val tagIds = item.tagIds?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
                val personIds = item.personIds?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
                val spent = transactionQueries.getSpentAmountByFilter(
                    categoryId = item.categoryId,
                    fromTimestamp = from,
                    toTimestamp = to,
                    sourceId = item.sourceId,
                    tagIds = tagIds,
                    tagIdsSize = tagIds.size.toLong(),
                    personIds = personIds,
                    personIdsSize = personIds.size.toLong()
                ).executeAsOne().SUM ?: 0L
                item.toBudgetWithProgress(spent)
            }
        }
    }

    override suspend fun getBudgetWithProgressByCategory(categoryId: Long): BudgetWithProgress? = withContext(Dispatchers.Default) {
        queries.observeBudgets().awaitAsList().find { it.categoryId == categoryId }?.let { item ->
            val now = Clock.System.now()
            val timeZone = TimeZone.currentSystemDefault()
            val pNow = now.toPersianDateTime(timeZone)
            val from = pNow.copy(day = 1, hour = 0, minute = 0, second = 0).toEpochMilliseconds(timeZone)
            val nextMonth = if (pNow.month == 12) PersianDateTime(pNow.year + 1, 1, 1) else PersianDateTime(pNow.year, pNow.month + 1, 1)
            val to = nextMonth.toEpochMilliseconds(timeZone)
            
            val tagIds = item.tagIds?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
            val personIds = item.personIds?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
            val spent = transactionQueries.getSpentAmountByFilter(
                categoryId = item.categoryId,
                fromTimestamp = from,
                toTimestamp = to,
                sourceId = item.sourceId,
                tagIds = tagIds,
                tagIdsSize = tagIds.size.toLong(),
                personIds = personIds,
                personIdsSize = personIds.size.toLong()
            ).executeAsOne().SUM ?: 0L
            item.toBudgetWithProgress(spent)
        }
    }

    override suspend fun getBudgetByCategoryId(categoryId: Long): Budget? = withContext(Dispatchers.Default) {
        // Warning: This could still return multiple rows if not filtered by period/startAt
        // For now, let's keep it but ideally use getBudgetById or similar if possible.
        queries.getBudgetByCategoryId(categoryId).executeAsList().firstOrNull()?.let {
            it.toModel()
        }
    }

    override suspend fun addBudget(budget: Budget): Long = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.insertBudget(
            categoryId = budget.categoryId,
            amount = budget.amount,
            period = budget.period.name,
            startAt = budget.startAt,
            tagIds = budget.tagIds?.joinToString(","),
            sourceId = budget.sourceId,
            personIds = budget.personIds?.joinToString(","),
            isAlertEnabled = if (budget.isAlertEnabled) 1L else 0L,
            updatedAt = now,
            currencyCode = budget.currencyCode,
            syncStatus = 1
        )
        queries.lastInsertRowId().executeAsOne()
    }

    override suspend fun updateBudget(budget: Budget): Int = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.updateBudget(
            id = budget.id!!,
            categoryId = budget.categoryId,
            amount = budget.amount,
            period = budget.period.name,
            startAt = budget.startAt,
            tagIds = budget.tagIds?.joinToString(","),
            sourceId = budget.sourceId,
            personIds = budget.personIds?.joinToString(","),
            isAlertEnabled = if (budget.isAlertEnabled) 1L else 0L,
            updatedAt = now,
            currencyCode = budget.currencyCode,
            syncStatus = 1
        )
        1
    }

    override suspend fun deleteBudget(id: Long) {
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.deleteBudget(now, id)
        }
    }

    override suspend fun getSpentAmountByCategory(categoryId: Long, from: Long, to: Long): Long {
        return transactionQueries.getSpentAmountByCategory(categoryId, from, to).executeAsOne().SUM ?: 0L
    }

    override suspend fun getAllBudgets(): List<Budget> = withContext(Dispatchers.Default) {
        queries.observeBudgets().awaitAsList().map {
            it.toModel()
        }
    }

    override suspend fun insertFullBudget(budget: Budget) {
        withContext(Dispatchers.Default) {
            queries.insertFullBudget(
                id = budget.id ?: 0,
                categoryId = budget.categoryId,
                amount = budget.amount,
                period = budget.period.name,
                startAt = budget.startAt,
                tagIds = budget.tagIds?.joinToString(","),
                sourceId = budget.sourceId,
                personIds = budget.personIds?.joinToString(","),
                isAlertEnabled = if (budget.isAlertEnabled) 1L else 0L,
                updatedAt = budget.updatedAt,
                syncStatus = budget.syncStatus.value.toLong()
            )
        }
    }

    override suspend fun getModifiedBudgets(): List<Budget> = withContext(Dispatchers.Default) {
        queries.getModifiedBudgets().awaitAsList().map {
            it.toModel()
        }
    }

    override suspend fun markBudgetAsSynced(id: Long) {
        queries.markBudgetAsSynced(id)
    }

    override suspend fun physicallyDeleteBudget(id: Long) {
        queries.physicallyDeleteBudget(id)
    }

    override suspend fun hasAnyBudgets(): Boolean = withContext(Dispatchers.Default) {
        queries.hasAnyBudgets().executeAsOne() > 0
    }

    private fun ObserveBudgets.toBudgetWithProgress(spent: Long): BudgetWithProgress {
        val budget = toModel()
        val category = categoryName?.let {
            Category(
                id = categoryId,
                name = it,
                colorId = categoryColorId?.toInt() ?: 1,
                iconId = categoryIconId?.toInt() ?: 1,
                type = TransactionType.fromInt(categoryType?.toInt() ?: TransactionType.EXPENSE.count)
            )
        }
        return BudgetWithProgress(
            budget = budget,
            category = category,
            spentAmount = spent,
            progress = if (budget.amount > 0) spent.toFloat() / budget.amount else 0f
        )
    }

    private fun ObserveBudgetsByRange.toBudgetWithProgress(spent: Long): BudgetWithProgress {
        val budget = toModel()
        val category = categoryName?.let {
            Category(
                id = categoryId,
                name = it,
                colorId = categoryColorId?.toInt() ?: 1,
                iconId = categoryIconId?.toInt() ?: 1,
                type = TransactionType.fromInt(categoryType?.toInt() ?: TransactionType.EXPENSE.count)
            )
        }
        return BudgetWithProgress(
            budget = budget,
            category = category,
            spentAmount = spent,
            progress = if (budget.amount > 0) spent.toFloat() / budget.amount else 0f
        )
    }

    private fun ObserveBudgets.toModel() = Budget(
        id = id,
        categoryId = categoryId,
        amount = amount,
        period = BudgetPeriod.valueOf(period),
        startAt = startAt,
        tagIds = tagIds?.split(",")?.mapNotNull { it.toLongOrNull() },
        sourceId = sourceId,
        personIds = personIds?.split(",")?.mapNotNull { it.toLongOrNull() },
        isAlertEnabled = isAlertEnabled == 1L,
        updatedAt = updatedAt,
        syncStatus = SyncStatus.fromInt(syncStatus.toInt())
    )

    private fun ObserveBudgetsByRange.toModel() = Budget(
        id = id,
        categoryId = categoryId,
        amount = amount,
        period = BudgetPeriod.valueOf(period),
        startAt = startAt,
        tagIds = tagIds?.split(",")?.mapNotNull { it.toLongOrNull() },
        sourceId = sourceId,
        personIds = personIds?.split(",")?.mapNotNull { it.toLongOrNull() },
        isAlertEnabled = isAlertEnabled == 1L,
        updatedAt = updatedAt,
        syncStatus = SyncStatus.fromInt(syncStatus.toInt())
    )

    private fun com.kazemieh.database.Budget.toModel() = Budget(
        id = id,
        categoryId = categoryId,
        amount = amount,
        period = BudgetPeriod.valueOf(period),
        startAt = startAt,
        tagIds = tagIds?.split(",")?.mapNotNull { it.toLongOrNull() },
        sourceId = sourceId,
        personIds = personIds?.split(",")?.mapNotNull { it.toLongOrNull() },
        isAlertEnabled = isAlertEnabled == 1L,
        updatedAt = updatedAt,
        syncStatus = SyncStatus.fromInt(syncStatus.toInt())
    )
}
