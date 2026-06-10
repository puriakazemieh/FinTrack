package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Budget
import com.kazemieh.common.model.BudgetWithProgress
import com.kazemieh.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow

class ObserveBudgetsWithProgressUseCase(private val repository: BudgetRepository) {
    operator fun invoke(): Flow<List<BudgetWithProgress>> = repository.observeBudgetsWithProgress()
}

class AddBudgetUseCase(private val repository: BudgetRepository) {
    suspend operator fun invoke(budget: Budget) = repository.addBudget(budget)
}

class UpdateBudgetUseCase(private val repository: BudgetRepository) {
    suspend operator fun invoke(budget: Budget) = repository.updateBudget(budget)
}

class DeleteBudgetUseCase(private val repository: BudgetRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteBudget(id)
}

class GetBudgetByCategoryIdUseCase(private val repository: BudgetRepository) {
    suspend operator fun invoke(categoryId: Long) = repository.getBudgetByCategoryId(categoryId)
}

class GetBudgetSpentAmountUseCase(private val repository: BudgetRepository) {
    suspend operator fun invoke(categoryId: Long, from: Long, to: Long) =
        repository.getSpentAmountByCategory(categoryId, from, to)
}
