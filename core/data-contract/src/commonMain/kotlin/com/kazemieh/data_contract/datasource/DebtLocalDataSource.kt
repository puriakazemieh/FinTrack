package com.kazemieh.data_contract.datasource

import com.kazemieh.common.model.Debt
import com.kazemieh.common.model.DebtWithRelations
import kotlinx.coroutines.flow.Flow

interface DebtLocalDataSource {
    fun observeAllDebts(): Flow<List<DebtWithRelations>>
    fun observeDebtsByPerson(personId: Long): Flow<List<DebtWithRelations>>
    suspend fun getDebtById(id: Long): Debt?
    suspend fun insertDebt(debt: Debt, tagIds: List<Long> = emptyList()): Long
    suspend fun updateDebt(debt: Debt, tagIds: List<Long> = emptyList()): Int
    suspend fun deleteDebt(id: Long)
    suspend fun settleDebt(id: Long)

    suspend fun getAllDebts(): List<Debt>
    suspend fun insertFullDebt(debt: Debt)
    suspend fun getModifiedDebts(): List<Debt>
    suspend fun markDebtAsSynced(id: Long)
    suspend fun physicallyDeleteDebt(id: Long)
}
