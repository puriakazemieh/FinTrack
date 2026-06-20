package com.kazemieh.data_contract.datasource

import com.kazemieh.common.model.*

interface SyncLocalDataSource {
    suspend fun getModifiedEntities(): Map<String, List<Any>>
    suspend fun markAsSynced(table: String, ids: List<Long>)
    suspend fun updateFromRemote(entities: Map<String, List<Any>>)
}
