package com.kazemieh.database.datasource

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kazemieh.common.model.Check
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.data_contract.datasource.CheckLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.mapper.toCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class CheckLocalDataSourceImpl(
    private val database: FinTrackDatabase
) : CheckLocalDataSource {
    private val queries = database.checkQueries

    override suspend fun insertCheck(check: Check): Long = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.insertCheck(
            amount = check.amount,
            date = check.date,
            dueDate = check.dueDate,
            status = check.status.name,
            personId = check.personId,
            photoPath = check.photoPath,
            description = check.description,
            isIncoming = if (check.isIncoming) 1L else 0L,
            updatedAt = now,
            syncStatus = 1
        )
        queries.lastInsertRowId().executeAsOne()
    }

    override suspend fun updateCheck(check: Check) {
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.updateCheck(
                amount = check.amount,
                date = check.date,
                dueDate = check.dueDate,
                status = check.status.name,
                personId = check.personId,
                photoPath = check.photoPath,
                description = check.description,
                isIncoming = if (check.isIncoming) 1L else 0L,
                updatedAt = now,
                syncStatus = 1,
                id = check.id
            )
        }
    }

    override suspend fun deleteCheck(id: Long) {
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.deleteCheck(now, id)
        }
    }

    override suspend fun getCheckById(id: Long): Check? = withContext(Dispatchers.Default) {
        queries.getCheckById(id).executeAsOneOrNull()?.toCheck()
    }

    override fun observeAllChecks(): Flow<List<Check>> {
        return queries.observeAllChecks().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toCheck() }
        }
    }

    override fun observeChecksByStatus(status: CheckStatus): Flow<List<Check>> {
        return queries.observeChecksByStatus(status.name).asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toCheck() }
        }
    }

    override suspend fun getAllChecks(): List<Check> = withContext(Dispatchers.Default) {
        queries.observeAllChecks().awaitAsList().map { it.toCheck() }
    }

    override suspend fun insertFullCheck(check: Check) {
        withContext(Dispatchers.Default) {
            queries.insertFullCheck(
                id = check.id,
                amount = check.amount,
                date = check.date,
                dueDate = check.dueDate,
                status = check.status.name,
                personId = check.personId,
                photoPath = check.photoPath,
                description = check.description,
                isIncoming = if (check.isIncoming) 1L else 0L,
                updatedAt = check.updatedAt,
                syncStatus = check.syncStatus.value.toLong()
            )
        }
    }

    override suspend fun getModifiedChecks(): List<Check> = withContext(Dispatchers.Default) {
        queries.getModifiedChecks().awaitAsList().map { it.toCheck() }
    }

    override suspend fun markCheckAsSynced(id: Long) {
        queries.markCheckAsSynced(id)
    }

    override suspend fun physicallyDeleteCheck(id: Long) {
        queries.physicallyDeleteCheck(id)
    }
}
