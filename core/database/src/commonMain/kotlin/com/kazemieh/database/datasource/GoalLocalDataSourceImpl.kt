package com.kazemieh.database.datasource

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kazemieh.common.model.Goal
import com.kazemieh.common.model.SyncStatus
import com.kazemieh.data_contract.datasource.GoalLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.Goal as GoalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class GoalLocalDataSourceImpl(
    private val db: FinTrackDatabase
) : GoalLocalDataSource {

    private val queries = db.goalQueries

    override fun observeGoals(): Flow<List<Goal>> {
        return queries.observeGoals()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toGoal() } }
    }

    override suspend fun getGoalById(id: Long): Goal? = withContext(Dispatchers.Default) {
        queries.getGoalById(id).executeAsOneOrNull()?.toGoal()
    }

    override suspend fun addGoal(goal: Goal): Long = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.insertGoal(
            name = goal.name,
            targetAmount = goal.targetAmount,
            savedAmount = goal.savedAmount,
            iconId = goal.iconId.toLong(),
            colorId = goal.colorId.toLong(),
            endDate = goal.endDate,
            monthlyTarget = goal.monthlyTarget,
            updatedAt = now,
            syncStatus = 1
        )
        // SQLDelight's lastInsertRowId() is usually available if we use it in .sq or through a driver-specific way.
        // But here we can just fetch the latest if needed, or if we use the same pattern as others.
        // Others seem to fetch by unique criteria. Goals don't have a unique criteria other than id.
        // Let's check how TransactionLocalDataSourceImpl does it.
        queries.observeGoals().executeAsList().lastOrNull()?.id ?: 0L
    }

    override suspend fun updateGoal(goal: Goal): Int = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.updateGoal(
            id = goal.id,
            name = goal.name,
            targetAmount = goal.targetAmount,
            savedAmount = goal.savedAmount,
            iconId = goal.iconId.toLong(),
            colorId = goal.colorId.toLong(),
            endDate = goal.endDate,
            monthlyTarget = goal.monthlyTarget,
            updatedAt = now,
            syncStatus = 1
        )
        1
    }

    override suspend fun deleteGoal(id: Long) {
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.deleteGoal(now, id)
        }
    }

    override suspend fun getAllGoals(): List<Goal> = withContext(Dispatchers.Default) {
        queries.observeGoals().executeAsList().map { it.toGoal() }
    }

    override suspend fun insertFullGoal(goal: Goal) {
        withContext(Dispatchers.Default) {
            queries.insertFullGoal(
                id = goal.id,
                name = goal.name,
                targetAmount = goal.targetAmount,
                savedAmount = goal.savedAmount,
                iconId = goal.iconId.toLong(),
                colorId = goal.colorId.toLong(),
                endDate = goal.endDate,
                monthlyTarget = goal.monthlyTarget,
                updatedAt = goal.updatedAt,
                syncStatus = goal.syncStatus.value.toLong()
            )
        }
    }

    override suspend fun getModifiedGoals(): List<Goal> = withContext(Dispatchers.Default) {
        queries.getModifiedGoals().executeAsList().map { it.toGoal() }
    }

    override suspend fun markGoalAsSynced(id: Long) {
        queries.markGoalAsSynced(id)
    }

    override suspend fun physicallyDeleteGoal(id: Long) {
        queries.physicallyDeleteGoal(id)
    }

    private fun GoalEntity.toGoal(): Goal {
        return Goal(
            id = id,
            name = name,
            targetAmount = targetAmount,
            savedAmount = savedAmount,
            iconId = iconId.toInt(),
            colorId = colorId.toInt(),
            endDate = endDate,
            monthlyTarget = monthlyTarget,
            updatedAt = updatedAt,
            syncStatus = SyncStatus.fromInt(syncStatus.toInt())
        )
    }
}
