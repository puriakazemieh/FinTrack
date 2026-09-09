package com.kazemieh.data.repository

import com.kazemieh.common.model.Installment
import com.kazemieh.common.model.InstallmentWithRelations
import com.kazemieh.data_contract.datasource.InstallmentLocalDataSource
import com.kazemieh.domain.repository.InstallmentRepository
import com.kazemieh.domain.repository.PreferenceRepository
import com.kazemieh.money.Currency
import kotlinx.coroutines.flow.Flow

class InstallmentRepositoryImpl(
    private val localDataSource: InstallmentLocalDataSource,
    private val preferenceRepository: PreferenceRepository
) : InstallmentRepository {
    override fun observeInstallments(): Flow<List<InstallmentWithRelations>> =
        localDataSource.observeInstallments()

    override suspend fun getInstallmentWithRelations(id: Long): InstallmentWithRelations? =
        localDataSource.getInstallmentWithRelations(id)

    override suspend fun getInstallmentById(id: Long): Installment? =
        localDataSource.getInstallmentById(id)

    override suspend fun insertInstallment(installment: Installment, tagIds: List<Long>, personIds: List<Long>): Long =
        localDataSource.insertInstallment(installment.copy(currencyCode = selectedCurrencyCode()), tagIds, personIds)

    override suspend fun updateInstallment(installment: Installment, tagIds: List<Long>, personIds: List<Long>) =
        localDataSource.updateInstallment(installment, tagIds, personIds)

    override suspend fun deleteInstallment(id: Long) =
        localDataSource.deleteInstallment(id)

    private fun selectedCurrencyCode(): String =
        Currency.valueOf(preferenceRepository.getString("PREF_CURRENCY", "IRT")).code
}
