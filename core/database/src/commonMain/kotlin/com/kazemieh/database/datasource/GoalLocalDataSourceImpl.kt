package com.kazemieh.database.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kazemieh.common.model.Goal
import com.kazemieh.common.model.GoalBasket
import com.kazemieh.common.model.GoalCategory
import com.kazemieh.common.model.GoalPriority
import com.kazemieh.common.model.GoalTemplate
import com.kazemieh.common.model.GoalType
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.model.SyncStatus
import com.kazemieh.data_contract.datasource.GoalLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.Goal as GoalEntity
import com.kazemieh.database.Goal_basket
import com.kazemieh.database.Goal_template
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
            startDate = goal.startDate,
            endDate = goal.endDate,
            recurrence = goal.recurrence.name,
            monthlyTarget = goal.monthlyTarget,
            type = goal.type.name,
            category = goal.category.name,
            priority = goal.priority.name,
            description = goal.description,
            templateId = goal.templateId,
            basketId = goal.basketId,
            updatedAt = now,
            syncStatus = 1,
            currencyCode = goal.currencyCode
        )
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
            startDate = goal.startDate,
            endDate = goal.endDate,
            recurrence = goal.recurrence.name,
            monthlyTarget = goal.monthlyTarget,
            type = goal.type.name,
            category = goal.category.name,
            priority = goal.priority.name,
            description = goal.description,
            templateId = goal.templateId,
            basketId = goal.basketId,
            updatedAt = now,
            syncStatus = 1,
            currencyCode = goal.currencyCode
        )
        1
    }

    override suspend fun deleteGoal(id: Long) {
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.deleteGoal(now, id)
        }
    }

    override fun observeGoalTemplates(): Flow<List<GoalTemplate>> {
        return queries.observeGoalTemplates()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toGoalTemplate() } }
    }

    override suspend fun addGoalTemplate(template: GoalTemplate): Long = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.insertGoalTemplate(
            name = template.name,
            systemType = template.systemType.name,
            iconId = template.iconId.toLong(),
            colorId = template.colorId.toLong(),
            updatedAt = now,
            syncStatus = 1
        )
        queries.observeGoalTemplates().executeAsList().lastOrNull()?.id ?: 0L
    }

    override suspend fun updateGoalTemplate(template: GoalTemplate): Int = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.updateGoalTemplate(
            id = template.id,
            name = template.name,
            systemType = template.systemType.name,
            iconId = template.iconId.toLong(),
            colorId = template.colorId.toLong(),
            updatedAt = now,
            syncStatus = 1
        )
        1
    }

    override suspend fun deleteGoalTemplate(id: Long) {
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.deleteGoalTemplate(now, id)
        }
    }

    override fun observeGoalBaskets(): Flow<List<GoalBasket>> {
        return queries.observeGoalBaskets()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toGoalBasket() } }
    }

    override suspend fun addGoalBasket(basket: GoalBasket): Long = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.insertGoalBasket(
            name = basket.name,
            iconId = basket.iconId.toLong(),
            colorId = basket.colorId.toLong(),
            updatedAt = now,
            syncStatus = 1
        )
        queries.observeGoalBaskets().executeAsList().lastOrNull()?.id ?: 0L
    }

    override suspend fun updateGoalBasket(basket: GoalBasket): Int = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.updateGoalBasket(
            id = basket.id,
            name = basket.name,
            iconId = basket.iconId.toLong(),
            colorId = basket.colorId.toLong(),
            updatedAt = now,
            syncStatus = 1
        )
        1
    }

    override suspend fun deleteGoalBasket(id: Long) {
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.deleteGoalBasket(now, id)
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
                startDate = goal.startDate,
                endDate = goal.endDate,
                recurrence = goal.recurrence.name,
                monthlyTarget = goal.monthlyTarget,
                type = goal.type.name,
                category = goal.category.name,
                priority = goal.priority.name,
                description = goal.description,
                templateId = goal.templateId,
                basketId = goal.basketId,
                updatedAt = goal.updatedAt,
                syncStatus = goal.syncStatus.value.toLong(),
                currencyCode = goal.currencyCode
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
            startDate = startDate,
            endDate = endDate,
            recurrence = try { RecurrenceType.valueOf(recurrence) } catch (e: Exception) { RecurrenceType.NONE },
            monthlyTarget = monthlyTarget,
            type = try { GoalType.valueOf(type) } catch (e: Exception) { GoalType.SAVINGS },
            category = try { GoalCategory.valueOf(category) } catch (e: Exception) { GoalCategory.SHORT_TERM },
            priority = try { GoalPriority.valueOf(priority) } catch (e: Exception) { GoalPriority.MEDIUM },
            description = description,
            templateId = templateId,
            basketId = basketId,
            updatedAt = updatedAt,
            syncStatus = SyncStatus.fromInt(syncStatus.toInt())
        )
    }

    private fun Goal_template.toGoalTemplate(): GoalTemplate {
        return GoalTemplate(
            id = id,
            name = name,
            systemType = try { GoalType.valueOf(systemType) } catch (e: Exception) { GoalType.SAVINGS },
            iconId = iconId.toInt(),
            colorId = colorId.toInt(),
            updatedAt = updatedAt,
            syncStatus = SyncStatus.fromInt(syncStatus.toInt())
        )
    }

    private fun Goal_basket.toGoalBasket(): GoalBasket {
        return GoalBasket(
            id = id,
            name = name,
            iconId = iconId.toInt(),
            colorId = colorId.toInt(),
            updatedAt = updatedAt,
            syncStatus = SyncStatus.fromInt(syncStatus.toInt())
        )
    }
}
