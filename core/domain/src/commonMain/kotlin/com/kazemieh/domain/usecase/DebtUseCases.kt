package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Debt
import com.kazemieh.common.model.DebtType
import com.kazemieh.common.model.DebtWithRelations
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.repository.DebtRepository
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

data class DebtUseCaseGroup(
    val addDebtUseCase: AddDebtUseCase,
    val observeDebtsUseCase: ObserveDebtsUseCase,
    val observeDebtsByPersonUseCase: ObserveDebtsByPersonUseCase,
    val settleDebtUseCase: SettleDebtUseCase,
    val deleteDebtUseCase: DeleteDebtUseCase,
    val updateDebtUseCase: UpdateDebtUseCase,
    val getDebtByIdUseCase: GetDebtByIdUseCase,
    val getDebtWithRelationsUseCase: GetDebtWithRelationsUseCase,
    val getPersonByIdUseCase: GetPersonByIdUseCase,
    val getSourceByIdUseCase: GetSourceByIdUseCase
)

class GetDebtByIdUseCase(private val repository: DebtRepository) {
    suspend operator fun invoke(id: Long) = repository.getDebtById(id)
}

class GetDebtWithRelationsUseCase(private val repository: DebtRepository) {
    suspend operator fun invoke(id: Long): DebtWithRelations? = repository.observeAllDebts()
        .map { it.find { d -> d.debt.id == id } }
        .firstOrNull()
}
// Note: Alternatively, add a direct query for it in DebtRepository.
// For now, let's keep it simple or check if DebtRepository has it.

class AddDebtUseCase(
    private val repository: DebtRepository,
    private val notificationScheduler: com.kazemieh.domain.notification.NotificationScheduler
) {
    suspend operator fun invoke(debt: Debt, tagIds: List<Long> = emptyList()): Long {
        val id = repository.insertDebt(debt, tagIds)
        if (debt.reminderEnabled && debt.dueDate != null) {
            notificationScheduler.scheduleReminder(
                id = "debt_$id",
                title = debt.personName ?: "یادآور بدهی",
                message = debt.description ?: "سررسید بدهی/طلب",
                scheduledTime = kotlinx.datetime.Instant.fromEpochMilliseconds(debt.dueDate!!)
                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()),
                channelId = "debt_reminders"
            )
        }
        return id
    }
}

class UpdateDebtUseCase(
    private val repository: DebtRepository,
    private val notificationScheduler: com.kazemieh.domain.notification.NotificationScheduler
) {
    suspend operator fun invoke(debt: Debt, tagIds: List<Long> = emptyList()): Int {
        val result = repository.updateDebt(debt, tagIds)
        if (debt.reminderEnabled && debt.dueDate != null) {
            notificationScheduler.scheduleReminder(
                id = "debt_${debt.id}",
                title = debt.personName ?: "یادآور بدهی",
                message = debt.description ?: "سررسید بدهی/طلب",
                scheduledTime = kotlinx.datetime.Instant.fromEpochMilliseconds(debt.dueDate!!)
                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()),
                channelId = "debt_reminders"
            )
        } else {
            notificationScheduler.cancelReminder("debt_${debt.id}")
        }
        return result
    }
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
    suspend operator fun invoke(
        debtId: Long,
        description: String,
        postAsTransaction: Boolean = false
    ) {
        val debtWithRelations = debtRepository.getDebtById(debtId) ?: return
        if (debtWithRelations.isSettled) return

        // 1. Create a transaction only when explicitly requested by the user.
        if (postAsTransaction) debtWithRelations.sourceId?.let { sourceId ->
            val transactionType = if (debtWithRelations.type == DebtType.OWED_TO_ME) {
                TransactionType.INCOME // Receiving money that was owed to me
            } else {
                TransactionType.EXPENSE // Paying money that I owed
            }

            val categoryId = debtWithRelations.categoryId ?: transactionRepository.getDefaultCategory(transactionType).id ?: 0L

            val transaction = Transaction(
                id = 0,
                amount = debtWithRelations.amount.toInt(),
                categoryId = categoryId,
                sourceId = sourceId,
                relatedDebtId = debtId,
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
