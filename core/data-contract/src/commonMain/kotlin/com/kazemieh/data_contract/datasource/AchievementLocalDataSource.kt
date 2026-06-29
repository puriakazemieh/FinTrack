package com.kazemieh.data_contract.datasource

import com.kazemieh.common.model.Achievement
import com.kazemieh.common.model.AchievementType
import com.kazemieh.common.model.Streak
import kotlinx.coroutines.flow.Flow

interface AchievementLocalDataSource {
    fun observeAchievements(): Flow<List<Achievement>>
    fun observeStreak(): Flow<Streak>
    suspend fun updateAchievementProgress(type: AchievementType, progress: Int, isUnlocked: Boolean, unlockTime: Long?)
    suspend fun unlockAchievement(type: AchievementType, unlockTime: Long)
    suspend fun updateStreak(streak: Streak)
    suspend fun getStreak(): Streak
    suspend fun initializeAchievements(achievements: List<Pair<AchievementType, Int>>)
    suspend fun getAchievement(type: AchievementType): Achievement?
}
