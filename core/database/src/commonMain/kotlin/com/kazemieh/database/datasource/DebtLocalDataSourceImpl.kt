package com.kazemieh.database.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kazemieh.common.model.Debt
import com.kazemieh.common.model.DebtType
import com.kazemieh.common.model.DebtWithRelations
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.data_contract.datasource.DebtLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DebtLocalDataSourceImpl(db: FinTrackDatabase) : DebtLocalDataSource {
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

    override suspend fun getDebtById(id: Long): Debt? {
        return queries.getDebtById(id).executeAsOneOrNull()?.toDebt()
    }

    override suspend fun insertDebt(debt: Debt): Long {
        queries.insertDebt(
            personId = debt.personId,
            amount = debt.amount,
            date = debt.date,
            dueDate = debt.dueDate,
            sourceId = debt.sourceId,
            description = debt.description,
            type = debt.type.value.toLong(),
            isSettled = if (debt.isSettled) 1L else 0L
        )
        return queries.lastInsertRowId().executeAsOne()
    }

    override suspend fun updateDebt(debt: Debt): Int {
        queries.updateDebt(
            personId = debt.personId,
            amount = debt.amount,
            date = debt.date,
            dueDate = debt.dueDate,
            sourceId = debt.sourceId,
            description = debt.description,
            type = debt.type.value.toLong(),
            isSettled = if (debt.isSettled) 1L else 0L,
            id = debt.id
        )
        return 1
    }

    override suspend fun deleteDebt(id: Long) {
        queries.deleteDebt(id)
    }

    override suspend fun settleDebt(id: Long) {
        queries.settleDebt(id)
    }

    private fun com.kazemieh.database.ObserveAllDebts.toDebtWithRelations(): DebtWithRelations {
        return DebtWithRelations(
            debt = Debt(
                id = id,
                personId = personId,
                amount = amount,
                date = date,
                dueDate = dueDate,
                sourceId = sourceId,
                description = description,
                type = DebtType.fromInt(type.toInt()),
                isSettled = isSettled == 1L
            ),
            person = Person(
                id = personId,
                name = personName,
                description = personDescription
            ),
            source = sourceId?.let {
                Source(
                    id = it,
                    name = sourceName ?: "",
                    colorId = sourceColorId?.toInt() ?: 0,
                    iconId = sourceIconId?.toInt() ?: 0
                )
            }
        )
    }

    private fun com.kazemieh.database.ObserveDebtsByPerson.toDebtWithRelations(): DebtWithRelations {
        return DebtWithRelations(
            debt = Debt(
                id = id,
                personId = personId,
                amount = amount,
                date = date,
                dueDate = dueDate,
                sourceId = sourceId,
                description = description,
                type = DebtType.fromInt(type.toInt()),
                isSettled = isSettled == 1L
            ),
            person = Person(
                id = personId,
                name = personName,
                description = personDescription
            ),
            source = sourceId?.let {
                Source(
                    id = it,
                    name = sourceName ?: "",
                    colorId = sourceColorId?.toInt() ?: 0,
                    iconId = sourceIconId?.toInt() ?: 0
                )
            }
        )
    }

    private fun com.kazemieh.database.Debt.toDebt(): Debt {
        return Debt(
            id = id,
            personId = personId,
            amount = amount,
            date = date,
            dueDate = dueDate,
            sourceId = sourceId,
            description = description,
            type = DebtType.fromInt(type.toInt()),
            isSettled = isSettled == 1L
        )
    }
}
