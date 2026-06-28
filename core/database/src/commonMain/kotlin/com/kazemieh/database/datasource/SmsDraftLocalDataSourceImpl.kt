package com.kazemieh.database.datasource

import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kazemieh.common.model.SmsDraft
import com.kazemieh.data_contract.datasource.SmsDraftLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.mapper.toSmsDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SmsDraftLocalDataSourceImpl(
    private val db: FinTrackDatabase
) : SmsDraftLocalDataSource {

    private val smsDraftQueries = db.smsDraftQueries

    override suspend fun insertSmsDraft(smsDraft: SmsDraft): Long = withContext(Dispatchers.Default) {
        smsDraftQueries.insertSmsDraft(
            sender = smsDraft.sender,
            body = smsDraft.body,
            amount = smsDraft.amount.toLong(),
            bankName = smsDraft.bankName,
            type = smsDraft.type.count.toLong(),
            sourceId = smsDraft.sourceId,
            sourceIdentifier = smsDraft.sourceIdentifier,
            timeStamp = smsDraft.timeStamp,
            date = smsDraft.date,
            categoryId = smsDraft.categoryId,
            confidence = smsDraft.confidence.toLong(),
            isUsed = if (smsDraft.isUsed) 1L else 0L
        )
        smsDraftQueries.lastInsertRowId().awaitAsOne()
    }

    override suspend fun updateSmsDraft(smsDraft: SmsDraft): Unit  = withContext(Dispatchers.Default) {
        smsDraftQueries.updateSmsDraft(
            sender = smsDraft.sender,
            body = smsDraft.body,
            amount = smsDraft.amount.toLong(),
            bankName = smsDraft.bankName,
            type = smsDraft.type.count.toLong(),
            sourceId = smsDraft.sourceId,
            sourceIdentifier = smsDraft.sourceIdentifier,
            timeStamp = smsDraft.timeStamp,
            date = smsDraft.date,
            categoryId = smsDraft.categoryId,
            confidence = smsDraft.confidence.toLong(),
            isUsed = if (smsDraft.isUsed) 1L else 0L,
            id = smsDraft.id
        )
    }

    override fun observeUnusedSmsDrafts(): Flow<List<SmsDraft>> {
        return smsDraftQueries.observeUnusedSmsDrafts()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toSmsDraft() } }
    }

    override suspend fun markSmsDraftAsUsed(id: Long): Unit = withContext(Dispatchers.Default) {
        smsDraftQueries.markSmsDraftAsUsed(id)
    }

    override suspend fun deleteSmsDraft(id: Long): Unit = withContext(Dispatchers.Default) {
        smsDraftQueries.deleteSmsDraft(id)
    }

    override suspend fun getSmsDraftById(id: Long): SmsDraft? = withContext(Dispatchers.Default) {
        smsDraftQueries.getSmsDraftById(id)
            .awaitAsOneOrNull()
            ?.toSmsDraft()
    }
}
