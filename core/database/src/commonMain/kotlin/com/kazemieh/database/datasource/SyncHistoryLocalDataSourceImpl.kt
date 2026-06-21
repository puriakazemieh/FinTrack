package com.kazemieh.database.datasource

import com.kazemieh.common.model.SyncHistory
import com.kazemieh.data_contract.datasource.SyncHistoryLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.mapper.toSyncHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncHistoryLocalDataSourceImpl(
    private val db: FinTrackDatabase
) : SyncHistoryLocalDataSource {

    private val queries = db.syncHistoryQueries

    override suspend fun addSyncHistory(history: SyncHistory) {
        withContext(Dispatchers.Default) {
            queries.insertSyncHistory(
                timestamp = history.timestamp,
                type = history.type.name,
                status = history.status.name,
                recordCount = history.recordCount.toLong(),
                insertedCount = history.insertedCount.toLong(),
                updatedCount = history.updatedCount.toLong(),
                errorMessage = history.errorMessage
            )
        }
    }

    override suspend fun getSyncHistory(limit: Long): List<SyncHistory> = withContext(Dispatchers.Default) {
        queries.getLatestSyncHistory(limit).executeAsList().map { it.toSyncHistory() }
    }
}
