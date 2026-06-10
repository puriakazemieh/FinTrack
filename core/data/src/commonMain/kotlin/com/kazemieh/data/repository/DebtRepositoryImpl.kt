package com.kazemieh.data.repository

import com.kazemieh.common.model.Debt
import com.kazemieh.common.model.DebtWithRelations
import com.kazemieh.data_contract.datasource.DebtLocalDataSource
import com.kazemieh.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow

class DebtRepositoryImpl(
    private val localDataSource: DebtLocalDataSource
) : DebtRepository {
    override fun observeAllDebts(): Flow<List<DebtWithRelations>> = localDataSource.observeAllDebts()

    override fun observeDebtsByPerson(personId: Long): Flow<List<DebtWithRelations>> =
        localDataSource.observeDebtsByPerson(personId)

    override suspend fun getDebtById(id: Long): Debt? = localDataSource.getDebtById(id)

    override suspend fun insertDebt(debt: Debt): Long = localDataSource.insertDebt(debt)

    override suspend fun updateDebt(debt: Debt): Int = localDataSource.updateDebt(debt)

    override suspend fun deleteDebt(id: Long) = localDataSource.deleteDebt(id)

    override suspend fun settleDebt(id: Long) = localDataSource.settleDebt(id)
}
