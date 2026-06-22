package com.kazemieh.data_contract.datasource

import com.kazemieh.common.model.SmsDraft
import kotlinx.coroutines.flow.Flow

interface SmsDraftLocalDataSource {
    suspend fun insertSmsDraft(smsDraft: SmsDraft): Long
    fun observeUnusedSmsDrafts(): Flow<List<SmsDraft>>
    suspend fun markSmsDraftAsUsed(id: Long)
    suspend fun deleteSmsDraft(id: Long)
    suspend fun getSmsDraftById(id: Long): SmsDraft?
}
