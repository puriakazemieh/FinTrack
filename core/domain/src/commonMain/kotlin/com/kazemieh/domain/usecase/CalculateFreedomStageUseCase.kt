package com.kazemieh.domain.usecase

import com.kazemieh.common.model.FreedomStage
import com.kazemieh.common.model.GoalType
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.repository.DebtRepository
import com.kazemieh.domain.repository.GoalRepository
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CalculateFreedomStageUseCase(
    private val goalRepository: GoalRepository,
    private val transactionRepository: TransactionRepository,
    private val debtRepository: DebtRepository
) {
    operator fun invoke(): Flow<FreedomStage> {
        return combine(
            goalRepository.observeGoals(),
            transactionRepository.observeCategorySums(TransactionFilterParams()),
            debtRepository.observeAllDebts()
        ) { goals, categorySums, debts ->
            val totalIncome = categorySums.filter { it.type == TransactionType.INCOME }.sumOf { it.totalAmount }
            val totalExpense = categorySums.filter { it.type == TransactionType.EXPENSE }.sumOf { it.totalAmount }
            
            val emergencyFundGoal = goals.find { it.type == GoalType.EMERGENCY_FUND }
            val hasEmergencyFund = (emergencyFundGoal?.progress ?: 0f) >= 1f
            
            val hasActiveDebts = debts.any { !it.debt.isSettled }
            
            when {
                totalIncome < totalExpense -> FreedomStage.DEPENDENCE
                totalIncome >= totalExpense && !hasEmergencyFund -> FreedomStage.SOLVENCY
                hasEmergencyFund && hasActiveDebts -> FreedomStage.STABILITY
                !hasActiveDebts && (emergencyFundGoal?.savedAmount ?: 0L) < (totalExpense * 6) -> FreedomStage.DEBT_FREEDOM
                else -> FreedomStage.SECURITY
            }
        }
    }
}
