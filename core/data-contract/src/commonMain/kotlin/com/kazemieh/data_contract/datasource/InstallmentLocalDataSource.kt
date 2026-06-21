package com.kazemieh.data_contract.datasource

import com.kazemieh.common.model.Installment
import com.kazemieh.common.model.InstallmentWithRelations
import kotlinx.coroutines.flow.Flow

interface InstallmentLocalDataSource {
    fun observeInstallments(): Flow<List<InstallmentWithRelations>>
    suspend fun getInstallmentById(id: Long): Installment?
    suspend fun insertInstallment(installment: Installment): Long
    suspend fun updateInstallment(installment: Installment)
    suspend fun deleteInstallment(id: Long)

    suspend fun getAllInstallments(): List<Installment>
    suspend fun insertFullInstallment(installment: Installment)
    suspend fun getModifiedInstallments(): List<Installment>
    suspend fun markInstallmentAsSynced(id: Long)
    suspend fun physicallyDeleteInstallment(id: Long)
}
