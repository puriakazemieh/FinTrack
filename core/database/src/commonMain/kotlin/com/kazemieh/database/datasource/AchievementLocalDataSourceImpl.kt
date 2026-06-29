package com.kazemieh.database.datasource

import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.kazemieh.common.model.Achievement
import com.kazemieh.common.model.AchievementType
import com.kazemieh.common.model.Streak
import com.kazemieh.data_contract.datasource.AchievementLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.mapper.toAchievement
import com.kazemieh.database.mapper.toStreak
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AchievementLocalDataSourceImpl(
    private val db: FinTrackDatabase
) : AchievementLocalDataSource {

    private val achievementQueries = db.achievementQueries

    override fun observeAchievements(): Flow<List<Achievement>> {
        return achievementQueries.getAchievements()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toAchievement() } }
    }

    override fun observeStreak(): Flow<Streak> {
        return achievementQueries.getStreak()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toStreak() ?: Streak() }
    }

    override suspend fun updateAchievementProgress(
        type: AchievementType,
        progress: Int,
        isUnlocked: Boolean,
        unlockTime: Long?
    ): Unit = withContext(Dispatchers.Default) {
        achievementQueries.updateAchievementProgress(
            progress = progress.toLong(),
            isUnlocked = isUnlocked,
            unlockTime = unlockTime,
            type = type.key
        )
    }

    override suspend fun unlockAchievement(type: AchievementType, unlockTime: Long): Unit =
        withContext(Dispatchers.Default) {
            achievementQueries.unlockAchievement(unlockTime, type.key)
        }

    override suspend fun updateStreak(streak: Streak): Unit = withContext(Dispatchers.Default) {
        achievementQueries.updateStreak(
            currentStreak = streak.currentStreak.toLong(),
            bestStreak = streak.bestStreak.toLong(),
            lastTransactionDate = streak.lastTransactionDate,
            xp = streak.xp.toLong(),
            level = streak.level.toLong()
        )
    }

    override suspend fun getStreak(): Streak = withContext(Dispatchers.Default) {
        achievementQueries.getStreak()
            .awaitAsOneOrNull()
            ?.toStreak() ?: Streak()
    }

    override suspend fun initializeAchievements(achievements: List<Pair<AchievementType, Int>>) =
        withContext(Dispatchers.Default) {
            db.transaction {
                achievements.forEach { (type, target) ->
                    achievementQueries.insertAchievement(type.key, target.toLong())
                }
            }
        }

    override suspend fun getAchievement(type: AchievementType): Achievement? =
        withContext(Dispatchers.Default) {
            achievementQueries.getAchievementByType(type.key)
                .awaitAsOneOrNull()
                ?.toAchievement()
        }
}
