package com.kazemieh.data_contract.datasource

import com.kazemieh.common.model.SyncHistory

interface SyncHistoryLocalDataSource {
    suspend fun addSyncHistory(history: SyncHistory)
    suspend fun getSyncHistory(limit: Long): List<SyncHistory>
}
