package com.kazemieh.domain.repository

import com.kazemieh.common.model.SmsDraft
import kotlinx.coroutines.flow.Flow

interface SmsDraftRepository {
    suspend fun addSmsDraft(smsDraft: SmsDraft): Long
    fun observeUnusedSmsDrafts(): Flow<List<SmsDraft>>
    /** Explicit ignore retains the unused draft for thirty days from the first action. */
    suspend fun ignoreSmsDraft(id: Long)
    suspend fun deleteExpiredIgnoredSmsDrafts()
    suspend fun markSmsDraftAsUsed(id: Long)
    suspend fun deleteSmsDraft(id: Long)
    suspend fun deleteUnusedSmsDrafts()
    /** Deletes only the requested drafts that are still unused and returns their actual IDs. */
    suspend fun deleteUnusedSmsDrafts(ids: List<Long>): List<Long>
    suspend fun getSmsDraftById(id: Long): SmsDraft?
    suspend fun updateSmsDraft(smsDraft: SmsDraft)
}
