package com.kazemieh.database.datasource

import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kazemieh.common.model.Installment
import com.kazemieh.common.model.InstallmentWithRelations
import com.kazemieh.data_contract.datasource.InstallmentLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.mapper.toInstallment
import com.kazemieh.database.mapper.toInstallmentWithRelations
import kotlinx.datetime.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class InstallmentLocalDataSourceImpl(
    private val db: FinTrackDatabase
) : InstallmentLocalDataSource {

    private val installmentQueries = db.installmentQueries

    override fun observeInstallments(): Flow<List<InstallmentWithRelations>> {
        return installmentQueries.observeInstallments()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toInstallmentWithRelations() } }
    }

    override suspend fun getInstallmentById(id: Long): Installment? = withContext(Dispatchers.Default) {
        installmentQueries.getInstallmentById(id)
            .awaitAsOneOrNull()
            ?.toInstallment()
    }

    override suspend fun insertInstallment(installment: Installment): Long = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        installmentQueries.insertInstallment(
            title = installment.title,
            totalAmount = installment.totalAmount,
            installmentAmount = installment.installmentAmount,
            totalInstallments = installment.totalInstallments.toLong(),
            paidInstallments = installment.paidInstallments.toLong(),
            categoryId = installment.categoryId,
            sourceId = installment.sourceId,
            startDate = installment.startDate,
            nextDueDate = installment.nextDueDate,
            frequency = installment.frequency.name,
            description = installment.description,
            isCompleted = if (installment.isCompleted) 1L else 0L,
            reminderEnabled = if (installment.reminderEnabled) 1L else 0L,
            updatedAt = now,
            syncStatus = 1
        )
        installmentQueries.lastInsertRowId().awaitAsOne()
    }

    override suspend fun updateInstallment(installment: Installment): Unit = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        installmentQueries.updateInstallment(
            title = installment.title,
            totalAmount = installment.totalAmount,
            installmentAmount = installment.installmentAmount,
            totalInstallments = installment.totalInstallments.toLong(),
            paidInstallments = installment.paidInstallments.toLong(),
            categoryId = installment.categoryId,
            sourceId = installment.sourceId,
            startDate = installment.startDate,
            nextDueDate = installment.nextDueDate,
            frequency = installment.frequency.name,
            description = installment.description,
            isCompleted = if (installment.isCompleted) 1L else 0L,
            reminderEnabled = if (installment.reminderEnabled) 1L else 0L,
            updatedAt = now,
            syncStatus = 1,
            id = installment.id
        )
    }

    override suspend fun deleteInstallment(id: Long): Unit = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        installmentQueries.deleteInstallment(now, id)
    }

    override suspend fun getAllInstallments(): List<Installment> = withContext(Dispatchers.Default) {
        installmentQueries.observeInstallments().awaitAsList().map { it.toInstallment() }
    }

    override suspend fun insertFullInstallment(installment: Installment) = withContext(Dispatchers.Default) {
        installmentQueries.insertFullInstallment(
            id = installment.id,
            title = installment.title,
            totalAmount = installment.totalAmount,
            installmentAmount = installment.installmentAmount,
            totalInstallments = installment.totalInstallments.toLong(),
            paidInstallments = installment.paidInstallments.toLong(),
            categoryId = installment.categoryId,
            sourceId = installment.sourceId,
            startDate = installment.startDate,
            nextDueDate = installment.nextDueDate,
            frequency = installment.frequency.name,
            description = installment.description,
            isCompleted = if (installment.isCompleted) 1L else 0L,
            reminderEnabled = if (installment.reminderEnabled) 1L else 0L,
            updatedAt = installment.updatedAt,
            syncStatus = installment.syncStatus.value.toLong()
        )
    }

    override suspend fun getModifiedInstallments(): List<Installment> = withContext(Dispatchers.Default) {
        installmentQueries.getModifiedInstallments().awaitAsList().map { it.toInstallment() }
    }

    override suspend fun markInstallmentAsSynced(id: Long) {
        installmentQueries.markInstallmentAsSynced(id)
    }
}
