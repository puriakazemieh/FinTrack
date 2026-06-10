package com.kazemieh.database.datasource

import com.kazemieh.common.model.Budget
import com.kazemieh.common.model.BudgetPeriod
import com.kazemieh.common.model.BudgetWithProgress
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import com.kazemieh.data_contract.datasource.BudgetLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.ObserveBudgets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList

class BudgetLocalDataSourceImpl(
    private val db: FinTrackDatabase
) : BudgetLocalDataSource {

    private val queries = db.budgetQueries
    private val transactionQueries = db.transactionQueries

    override fun observeBudgetsWithProgress(): Flow<List<BudgetWithProgress>> {
        return queries.observeBudgets().asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { item ->
                val spent = getSpentAmount(item.categoryId, item.period, item.startAt)
                item.toBudgetWithProgress(spent)
            }
        }
    }

    private fun getSpentAmount(categoryId: Long, periodStr: String, startAt: Long): Long {
        val period = BudgetPeriod.valueOf(periodStr)
        // This is a simplified calculation of the range.
        // In a real app, you'd use JalaliCalendar to find the start and end of the current period.
        val from = startAt // Placeholder
        val to = Long.MAX_VALUE // Placeholder
        return transactionQueries.getSpentAmountByCategory(categoryId, from, to).executeAsOne().SUM ?: 0L
    }

    override suspend fun getBudgetByCategoryId(categoryId: Long): Budget? {
        return queries.getBudgetByCategoryId(categoryId).executeAsOneOrNull()?.let {
            Budget(
                id = it.id,
                categoryId = it.categoryId,
                amount = it.amount,
                period = BudgetPeriod.valueOf(it.period),
                startAt = it.startAt,
                isAlertEnabled = it.isAlertEnabled == 1L
            )
        }
    }

    override suspend fun addBudget(budget: Budget): Long {
        queries.insertBudget(
            categoryId = budget.categoryId,
            amount = budget.amount,
            period = budget.period.name,
            startAt = budget.startAt,
            isAlertEnabled = if (budget.isAlertEnabled) 1L else 0L
        )
        return queries.getBudgetByCategoryId(budget.categoryId).executeAsOne().id
    }

    override suspend fun updateBudget(budget: Budget): Int {
        queries.updateBudget(
            id = budget.id!!,
            categoryId = budget.categoryId,
            amount = budget.amount,
            period = budget.period.name,
            startAt = budget.startAt,
            isAlertEnabled = if (budget.isAlertEnabled) 1L else 0L
        )
        return 1
    }

    override suspend fun deleteBudget(id: Long) {
        queries.deleteBudget(id)
    }

    override suspend fun getSpentAmountByCategory(categoryId: Long, from: Long, to: Long): Long {
        return transactionQueries.getSpentAmountByCategory(categoryId, from, to).executeAsOne().SUM ?: 0L
    }

    private fun ObserveBudgets.toBudgetWithProgress(spent: Long): BudgetWithProgress {
        val budget = Budget(
            id = id,
            categoryId = categoryId,
            amount = amount,
            period = BudgetPeriod.valueOf(period),
            startAt = startAt,
            isAlertEnabled = isAlertEnabled == 1L
        )
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
}
