package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Debt
import com.kazemieh.common.model.DebtType
import com.kazemieh.common.model.DebtWithRelations
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.repository.DebtRepository
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

data class DebtUseCaseGroup(
    val addDebtUseCase: AddDebtUseCase,
    val observeDebtsUseCase: ObserveDebtsUseCase,
    val observeDebtsByPersonUseCase: ObserveDebtsByPersonUseCase,
    val settleDebtUseCase: SettleDebtUseCase,
    val deleteDebtUseCase: DeleteDebtUseCase,
    val updateDebtUseCase: UpdateDebtUseCase,
    val getDebtByIdUseCase: GetDebtByIdUseCase,
    val getPersonByIdUseCase: GetPersonByIdUseCase,
    val getSourceByIdUseCase: GetSourceByIdUseCase
)

class GetDebtByIdUseCase(private val repository: DebtRepository) {
    suspend operator fun invoke(id: Long) = repository.getDebtById(id)
}

class AddDebtUseCase(private val repository: DebtRepository) {
    suspend operator fun invoke(debt: Debt): Long = repository.insertDebt(debt)
}

class UpdateDebtUseCase(private val repository: DebtRepository) {
    suspend operator fun invoke(debt: Debt): Int = repository.updateDebt(debt)
}

class ObserveDebtsUseCase(private val repository: DebtRepository) {
    operator fun invoke(): Flow<List<DebtWithRelations>> = repository.observeAllDebts()
}

class ObserveDebtsByPersonUseCase(private val repository: DebtRepository) {
    operator fun invoke(personId: Long): Flow<List<DebtWithRelations>> =
        repository.observeDebtsByPerson(personId)
}

class DeleteDebtUseCase(private val repository: DebtRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteDebt(id)
}

class SettleDebtUseCase(
    private val debtRepository: DebtRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(debtId: Long, description: String) {
        val debtWithRelations = debtRepository.getDebtById(debtId) ?: return
        if (debtWithRelations.isSettled) return

        // 1. Create Transaction if source is selected
        debtWithRelations.sourceId?.let { sourceId ->
            val transactionType = if (debtWithRelations.type == DebtType.OWED_TO_ME) {
                TransactionType.INCOME // Receiving money that was owed to me
            } else {
                TransactionType.EXPENSE // Paying money that I owed
            }

            val category = transactionRepository.getDefaultCategory(transactionType)

            val transaction = Transaction(
                id = 0,
                amount = debtWithRelations.amount.toInt(),
                categoryId = category.id ?: 0L,
                sourceId = sourceId,
                description = description,
                type = transactionType,
                timeStamp = Clock.System.now().toEpochMilliseconds()
            )

            transactionRepository.addTransactionWithBalance(
                transaction = transaction,
                tagIds = emptyList(),
                personIds = listOf(debtWithRelations.personId),
                balanceDeltas = mapOf(sourceId to (if (transactionType == TransactionType.INCOME) debtWithRelations.amount else -debtWithRelations.amount).toInt())
            )
        }

        // 2. Mark Debt as settled
        debtRepository.settleDebt(debtId)
    }
}
