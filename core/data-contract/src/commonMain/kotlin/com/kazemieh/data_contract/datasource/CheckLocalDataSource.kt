package com.kazemieh.data_contract.datasource

import com.kazemieh.common.model.Check
import com.kazemieh.common.model.CheckStatus
import kotlinx.coroutines.flow.Flow

interface CheckLocalDataSource {
    suspend fun insertCheck(check: Check): Long
    suspend fun updateCheck(check: Check)
    suspend fun deleteCheck(id: Long)
    suspend fun getCheckById(id: Long): Check?
    fun observeAllChecks(): Flow<List<Check>>
    fun observeChecksByStatus(status: CheckStatus): Flow<List<Check>>
}
