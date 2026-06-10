package com.kazemieh.database.datasource

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

class CheckLocalDataSourceImpl(
    private val database: FinTrackDatabase
) : CheckLocalDataSource {
    private val queries = database.checkQueries

    override suspend fun insertCheck(check: Check): Long {
        queries.insertCheck(
            amount = check.amount,
            date = check.date,
            dueDate = check.dueDate,
            status = check.status.name,
            personId = check.personId,
            photoPath = check.photoPath,
            description = check.description,
            isIncoming = if (check.isIncoming) 1L else 0L
        )
        return queries.lastInsertRowId().executeAsOne()
    }

    override suspend fun updateCheck(check: Check) {
        queries.updateCheck(
            amount = check.amount,
            date = check.date,
            dueDate = check.dueDate,
            status = check.status.name,
            personId = check.personId,
            photoPath = check.photoPath,
            description = check.description,
            isIncoming = if (check.isIncoming) 1L else 0L,
            id = check.id
        )
    }

    override suspend fun deleteCheck(id: Long) {
        queries.deleteCheck(id)
    }

    override suspend fun getCheckById(id: Long): Check? {
        return queries.getCheckById(id).executeAsOneOrNull()?.toCheck()
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
}
