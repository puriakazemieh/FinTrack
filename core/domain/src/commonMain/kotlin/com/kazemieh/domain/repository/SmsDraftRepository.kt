package com.kazemieh.domain.repository

import com.kazemieh.common.model.SmsDraft
import kotlinx.coroutines.flow.Flow

interface SmsDraftRepository {
    suspend fun addSmsDraft(smsDraft: SmsDraft): Long
    fun observeUnusedSmsDrafts(): Flow<List<SmsDraft>>
    suspend fun markSmsDraftAsUsed(id: Long)
    suspend fun deleteSmsDraft(id: Long)
    suspend fun getSmsDraftById(id: Long): SmsDraft?
}
