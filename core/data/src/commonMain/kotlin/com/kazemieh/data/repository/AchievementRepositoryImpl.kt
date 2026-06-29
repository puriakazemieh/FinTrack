package com.kazemieh.data.repository

import com.kazemieh.common.model.Achievement
import com.kazemieh.common.model.AchievementType
import com.kazemieh.common.model.Streak
import com.kazemieh.data_contract.datasource.AchievementLocalDataSource
import com.kazemieh.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

class AchievementRepositoryImpl(
    private val localDataSource: AchievementLocalDataSource
) : AchievementRepository {

    override fun observeAchievements(): Flow<List<Achievement>> =
        localDataSource.observeAchievements()

    override fun observeStreak(): Flow<Streak> =
        localDataSource.observeStreak()

    override suspend fun updateAchievementProgress(
        type: AchievementType,
        progress: Int,
        isUnlocked: Boolean
    ) {
        val unlockTime = if (isUnlocked) Clock.System.now().toEpochMilliseconds() else null
        localDataSource.updateAchievementProgress(type, progress, isUnlocked, unlockTime)
    }

    override suspend fun unlockAchievement(type: AchievementType) {
        localDataSource.unlockAchievement(type, Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun updateStreak(streak: Streak) {
        localDataSource.updateStreak(streak)
    }

    override suspend fun getStreak(): Streak =
        localDataSource.getStreak()

    override suspend fun initializeAchievements(achievements: List<Pair<AchievementType, Int>>) {
        localDataSource.initializeAchievements(achievements)
    }

    override suspend fun getAchievement(type: AchievementType): Achievement? =
        localDataSource.getAchievement(type)
}
