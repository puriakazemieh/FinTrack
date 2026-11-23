package com.kazemieh.data_contract.datasource

import com.kazemieh.common.model.SmsDraft
import kotlinx.coroutines.flow.Flow

interface SmsDraftLocalDataSource {
    suspend fun insertSmsDraft(smsDraft: SmsDraft): Long
    fun observeUnusedSmsDrafts(): Flow<List<SmsDraft>>
    suspend fun ignoreSmsDraft(id: Long, ignoredAt: Long)
    suspend fun deleteExpiredIgnoredSmsDrafts(cutoff: Long)
    suspend fun markSmsDraftAsUsed(id: Long)
    suspend fun deleteSmsDraft(id: Long)
    suspend fun deleteUnusedSmsDrafts()
    suspend fun deleteUnusedSmsDrafts(ids: List<Long>): List<Long>
    suspend fun getSmsDraftById(id: Long): SmsDraft?
    suspend fun updateSmsDraft(smsDraft: SmsDraft)
}
