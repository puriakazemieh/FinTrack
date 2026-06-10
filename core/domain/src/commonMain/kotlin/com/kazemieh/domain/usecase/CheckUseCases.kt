package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Check
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.domain.repository.CheckRepository
import kotlinx.coroutines.flow.Flow

class AddCheckUseCase(private val repository: CheckRepository) {
    suspend operator fun invoke(check: Check) = repository.insertCheck(check)
}

class UpdateCheckUseCase(private val repository: CheckRepository) {
    suspend operator fun invoke(check: Check) = repository.updateCheck(check)
}

class DeleteCheckUseCase(private val repository: CheckRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteCheck(id)
}

class GetCheckByIdUseCase(private val repository: CheckRepository) {
    suspend operator fun invoke(id: Long) = repository.getCheckById(id)
}

class ObserveAllChecksUseCase(private val repository: CheckRepository) {
    operator fun invoke(): Flow<List<Check>> = repository.observeAllChecks()
}

class ObserveChecksByStatusUseCase(private val repository: CheckRepository) {
    operator fun invoke(status: CheckStatus): Flow<List<Check>> = repository.observeChecksByStatus(status)
}

data class CheckUseCaseGroup(
    val addCheckUseCase: AddCheckUseCase,
    val updateCheckUseCase: UpdateCheckUseCase,
    val deleteCheckUseCase: DeleteCheckUseCase,
    val getCheckByIdUseCase: GetCheckByIdUseCase,
    val observeAllChecksUseCase: ObserveAllChecksUseCase,
    val observeChecksByStatusUseCase: ObserveChecksByStatusUseCase
)
