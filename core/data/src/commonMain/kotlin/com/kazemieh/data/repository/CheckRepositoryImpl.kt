package com.kazemieh.data.repository

import com.kazemieh.common.model.Check
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.data_contract.datasource.CheckLocalDataSource
import com.kazemieh.domain.repository.CheckRepository
import kotlinx.coroutines.flow.Flow

class CheckRepositoryImpl(
    private val localDataSource: CheckLocalDataSource
) : CheckRepository {
    override suspend fun insertCheck(check: Check): Long = localDataSource.insertCheck(check)
    override suspend fun updateCheck(check: Check) = localDataSource.updateCheck(check)
    override suspend fun deleteCheck(id: Long) = localDataSource.deleteCheck(id)
    override suspend fun getCheckById(id: Long): Check? = localDataSource.getCheckById(id)
    override fun observeAllChecks(): Flow<List<Check>> = localDataSource.observeAllChecks()
    override fun observeChecksByStatus(status: CheckStatus): Flow<List<Check>> = localDataSource.observeChecksByStatus(status)
}
