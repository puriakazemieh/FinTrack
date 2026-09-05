package com.kazemieh.database.datasource

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kazemieh.common.model.Debt
import com.kazemieh.common.model.DebtWithRelations
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.SyncStatus
import com.kazemieh.common.model.DebtType
import com.kazemieh.data_contract.datasource.DebtLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.mapper.toDebt
import com.kazemieh.database.mapper.toDebtWithRelations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class DebtLocalDataSourceImpl(private val db: FinTrackDatabase) : DebtLocalDataSource {
    private val queries = db.debtQueries

    override fun observeAllDebts(): Flow<List<DebtWithRelations>> {
        return queries.observeAllDebts().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toDebtWithRelations() }
        }
    }

    override fun observeDebtsByPerson(personId: Long): Flow<List<DebtWithRelations>> {
        return queries.observeDebtsByPerson(personId).asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toDebtWithRelations() }
        }
    }

    override suspend fun getDebtById(id: Long): Debt? = withContext(Dispatchers.Default) {
        queries.getDebtById(id).executeAsOneOrNull()?.toDebt()
    }

    override suspend fun insertDebt(debt: Debt, tagIds: List<Long>): Long = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        db.transaction {
            queries.insertDebt(
                personId = debt.personId,
                amount = debt.amount,
                categoryId = debt.categoryId,
                date = debt.date,
                dueDate = debt.dueDate,
                sourceId = debt.sourceId,
                description = debt.description,
                type = debt.type.value.toLong(),
                isSettled = if (debt.isSettled) 1L else 0L,
                reminderEnabled = if (debt.reminderEnabled) 1L else 0L,
                updatedAt = now,
            currencyCode = debt.currencyCode,
                syncStatus = 1
            )
            val id = queries.lastInsertRowId().executeAsOne()
            tagIds.forEach { tagId ->
                db.debtTagQueries.insertDebtTag(id, tagId)
            }
        }
        queries.lastInsertRowId().executeAsOne()
    }

    override suspend fun updateDebt(debt: Debt, tagIds: List<Long>): Int = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        db.transaction {
            queries.updateDebt(
                personId = debt.personId,
                amount = debt.amount,
                categoryId = debt.categoryId,
                date = debt.date,
                dueDate = debt.dueDate,
                sourceId = debt.sourceId,
                description = debt.description,
                type = debt.type.value.toLong(),
                isSettled = if (debt.isSettled) 1L else 0L,
                reminderEnabled = if (debt.reminderEnabled) 1L else 0L,
                updatedAt = now,
            currencyCode = debt.currencyCode,
                syncStatus = 1,
                id = debt.id
            )
            db.debtTagQueries.deleteTagsByDebtId(debt.id)
            tagIds.forEach { tagId ->
                db.debtTagQueries.insertDebtTag(debt.id, tagId)
            }
        }
        1
    }

    override suspend fun deleteDebt(id: Long) {
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.deleteDebt(now, id)
        }
    }

    override suspend fun settleDebt(id: Long) {
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.settleDebt(now, 1, id)
        }
    }

    override suspend fun getAllDebts(): List<Debt> = withContext(Dispatchers.Default) {
        queries.observeAllDebts().awaitAsList().map { it.toDebt() }
    }

    override suspend fun insertFullDebt(debt: Debt) {
        withContext(Dispatchers.Default) {
            queries.insertFullDebt(
                id = debt.id,
                personId = debt.personId,
                amount = debt.amount,
                categoryId = debt.categoryId,
                date = debt.date,
                dueDate = debt.dueDate,
                sourceId = debt.sourceId,
                description = debt.description,
                type = debt.type.value.toLong(),
                isSettled = if (debt.isSettled) 1L else 0L,
                reminderEnabled = if (debt.reminderEnabled) 1L else 0L,
                updatedAt = debt.updatedAt,
                syncStatus = debt.syncStatus.value.toLong(),
                currencyCode = debt.currencyCode
            )
        }
    }

    override suspend fun getModifiedDebts(): List<Debt> = withContext(Dispatchers.Default) {
        queries.getModifiedDebts().awaitAsList().map { it.toDebt() }
    }

    override suspend fun markDebtAsSynced(id: Long) {
        queries.markDebtAsSynced(id)
    }

    override suspend fun physicallyDeleteDebt(id: Long) {
        queries.physicallyDeleteDebt(id)
    }
}
