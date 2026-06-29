package com.kazemieh.domain.repository

import com.kazemieh.common.model.Achievement
import com.kazemieh.common.model.AchievementType
import com.kazemieh.common.model.Streak
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    fun observeAchievements(): Flow<List<Achievement>>
    fun observeStreak(): Flow<Streak>
    suspend fun updateAchievementProgress(type: AchievementType, progress: Int, isUnlocked: Boolean)
    suspend fun unlockAchievement(type: AchievementType)
    suspend fun updateStreak(streak: Streak)
    suspend fun getStreak(): Streak
    suspend fun initializeAchievements(achievements: List<Pair<AchievementType, Int>>)
    suspend fun getAchievement(type: AchievementType): Achievement?
}
