package com.kazemieh.domain.usecase

import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.repository.AssetRepository
import com.kazemieh.domain.repository.BudgetRepository
import com.kazemieh.domain.repository.DebtRepository
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.domain.repository.GoalRepository
import com.kazemieh.domain.repository.InstallmentRepository
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlin.time.Clock

data class FinancialSummary(
    val totalBalance: Long,
    val totalAssets: Long,
    val assetBreakdown: Map<String, Long>,
    val totalDebt: Long,
    val activeBudgetsCount: Int,
    val exceededBudgetsCount: Int,
    val monthlyIncome: Long,
    val monthlyExpense: Long,
    val prevMonthlyIncome: Long,
    val prevMonthlyExpense: Long,
    val topExpenseCategories: List<Pair<String, Long>>,
    val upcomingInstallments: List<Pair<String, Long>>,
    val upcomingFixedExpenses: List<Pair<String, Long>>,
    val goalProgress: List<Pair<String, Int>>
)

class GetFinancialSummaryUseCase(
    private val transactionRepository: TransactionRepository,
    private val assetRepository: AssetRepository,
    private val debtRepository: DebtRepository,
    private val budgetRepository: BudgetRepository,
    private val installmentRepository: InstallmentRepository,
    private val fixedExpenseRepository: FixedExpenseRepository,
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(): FinancialSummary {
        val now = Clock.System.now().toEpochMilliseconds()
        val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
        val sixtyDaysAgo = now - (60L * 24 * 60 * 60 * 1000)
        
        val monthFilter = TransactionFilterParams(fromTimestamp = thirtyDaysAgo, toTimestamp = now)
        val prevMonthFilter = TransactionFilterParams(fromTimestamp = sixtyDaysAgo, toTimestamp = thirtyDaysAgo)

        // 1. Total Balance from all sources
        val sources = transactionRepository.observeSources().first()
        val totalBalance = sources.sumOf { it.balance.toLong() }

        // 2. Total Assets and Breakdown
        val assets = assetRepository.observeAssets().first()
        val totalAssetsValue = assets.sumOf { it.totalCurrentValue }
        val assetBreakdown = assets.groupBy { it.type.name }
            .mapValues { entry -> entry.value.sumOf { it.totalCurrentValue } }

        // 3. Total Outstanding Debt
        val debts = debtRepository.observeAllDebts().first()
        val totalDebt = debts.filter { !it.debt.isSettled }.sumOf { it.debt.amount }

        // 4. Budgets status
        val budgets = budgetRepository.observeBudgetsWithProgress(thirtyDaysAgo, now).first()
        val activeBudgets = budgets.size
        val exceededBudgets = budgets.count { it.spentAmount > it.budget.amount }

        // 5. Monthly Flow & Trends
        val currentSums = transactionRepository.observeCategorySums(monthFilter).first()
        val prevSums = transactionRepository.observeCategorySums(prevMonthFilter).first()
        
        val income = currentSums.filter { it.type == TransactionType.INCOME }.sumOf { it.totalAmount }
        val expense = currentSums.filter { it.type == TransactionType.EXPENSE }.sumOf { it.totalAmount }
        
        val prevIncome = prevSums.filter { it.type == TransactionType.INCOME }.sumOf { it.totalAmount }
        val prevExpense = prevSums.filter { it.type == TransactionType.EXPENSE }.sumOf { it.totalAmount }

        val topExpenseCategories = currentSums
            .filter { it.type == TransactionType.EXPENSE }
            .sortedByDescending { it.totalAmount }
            .take(3)
            .map { it.name to it.totalAmount }

        // 6. Upcoming Obligations (Next 14 days)
        val fourteenDaysLater = now + (14L * 24 * 60 * 60 * 1000)
        
        val upcomingInstallments = installmentRepository.observeInstallments().first()
            .filter { it.installment.nextDueDate in (now..fourteenDaysLater) && !it.installment.isCompleted }
            .map { it.installment.title to it.installment.installmentAmount }
            
        val upcomingFixedExpenses = fixedExpenseRepository.observeAllFixedExpenses().first()
            .filter { it.nextDueDate in (now..fourteenDaysLater) && it.isActive }
            .map { it.title to it.amount }

        // 7. Goals
        val goalProgress = goalRepository.observeGoals().first()
            .filter { it.progress < 1.0f }
            .map { it.name to it.percent }

        return FinancialSummary(
            totalBalance = totalBalance,
            totalAssets = totalAssetsValue,
            assetBreakdown = assetBreakdown,
            totalDebt = totalDebt,
            activeBudgetsCount = activeBudgets,
            exceededBudgetsCount = exceededBudgets,
            monthlyIncome = income,
            monthlyExpense = expense,
            prevMonthlyIncome = prevIncome,
            prevMonthlyExpense = prevExpense,
            topExpenseCategories = topExpenseCategories,
            upcomingInstallments = upcomingInstallments,
            upcomingFixedExpenses = upcomingFixedExpenses,
            goalProgress = goalProgress
        )
    }
}
