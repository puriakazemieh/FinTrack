package com.kazemieh.domain.repository

import com.kazemieh.common.model.Debt
import com.kazemieh.common.model.DebtWithRelations
import kotlinx.coroutines.flow.Flow

interface DebtRepository {
    fun observeAllDebts(): Flow<List<DebtWithRelations>>
    fun observeDebtsByPerson(personId: Long): Flow<List<DebtWithRelations>>
    suspend fun getDebtById(id: Long): Debt?
    suspend fun insertDebt(debt: Debt, tagIds: List<Long> = emptyList()): Long
    suspend fun updateDebt(debt: Debt, tagIds: List<Long> = emptyList()): Int
    suspend fun deleteDebt(id: Long): Unit
    suspend fun settleDebt(id: Long): Unit
}
