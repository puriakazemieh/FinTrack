package com.kazemieh.domain.usecase

import com.kazemieh.common.model.FixedExpense
import com.kazemieh.domain.repository.FixedExpenseRepository
import kotlinx.coroutines.flow.Flow

class AddFixedExpenseUseCase(private val repository: FixedExpenseRepository) {
    suspend operator fun invoke(expense: FixedExpense) = repository.insertFixedExpense(expense)
}

class UpdateFixedExpenseUseCase(private val repository: FixedExpenseRepository) {
    suspend operator fun invoke(expense: FixedExpense) = repository.updateFixedExpense(expense)
}

class DeleteFixedExpenseUseCase(private val repository: FixedExpenseRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteFixedExpense(id)
}

class GetFixedExpenseByIdUseCase(private val repository: FixedExpenseRepository) {
    suspend operator fun invoke(id: Long) = repository.getFixedExpenseById(id)
}

class ObserveAllFixedExpensesUseCase(private val repository: FixedExpenseRepository) {
    operator fun invoke(): Flow<List<FixedExpense>> = repository.observeAllFixedExpenses()
}

class UpdateNextDueDateUseCase(private val repository: FixedExpenseRepository) {
    suspend operator fun invoke(id: Long, nextDueDate: Long) = repository.updateNextDueDate(id, nextDueDate)
}

data class FixedExpenseUseCaseGroup(
    val addFixedExpenseUseCase: AddFixedExpenseUseCase,
    val updateFixedExpenseUseCase: UpdateFixedExpenseUseCase,
    val deleteFixedExpenseUseCase: DeleteFixedExpenseUseCase,
    val getFixedExpenseByIdUseCase: GetFixedExpenseByIdUseCase,
    val observeAllFixedExpensesUseCase: ObserveAllFixedExpensesUseCase,
    val updateNextDueDateUseCase: UpdateNextDueDateUseCase
)
