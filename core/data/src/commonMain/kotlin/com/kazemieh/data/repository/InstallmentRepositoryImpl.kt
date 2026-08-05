package com.kazemieh.data.repository

import com.kazemieh.common.model.Installment
import com.kazemieh.common.model.InstallmentWithRelations
import com.kazemieh.data_contract.datasource.InstallmentLocalDataSource
import com.kazemieh.domain.repository.InstallmentRepository
import kotlinx.coroutines.flow.Flow

class InstallmentRepositoryImpl(
    private val localDataSource: InstallmentLocalDataSource
) : InstallmentRepository {
    override fun observeInstallments(): Flow<List<InstallmentWithRelations>> =
        localDataSource.observeInstallments()

    override suspend fun getInstallmentWithRelations(id: Long): InstallmentWithRelations? =
        localDataSource.getInstallmentWithRelations(id)

    override suspend fun getInstallmentById(id: Long): Installment? =
        localDataSource.getInstallmentById(id)

    override suspend fun insertInstallment(installment: Installment, tagIds: List<Long>, personIds: List<Long>): Long =
        localDataSource.insertInstallment(installment, tagIds, personIds)

    override suspend fun updateInstallment(installment: Installment, tagIds: List<Long>, personIds: List<Long>) =
        localDataSource.updateInstallment(installment, tagIds, personIds)

    override suspend fun deleteInstallment(id: Long) =
        localDataSource.deleteInstallment(id)
}
