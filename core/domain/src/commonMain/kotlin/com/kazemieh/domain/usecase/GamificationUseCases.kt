package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Achievement
import com.kazemieh.common.model.AchievementType
import com.kazemieh.common.model.Streak
import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.domain.repository.AchievementRepository
import com.kazemieh.domain.repository.BudgetRepository
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlin.math.max
import kotlin.time.Clock

class ObserveStreakUseCase(private val repository: AchievementRepository) {
    operator fun invoke(): Flow<Streak> = repository.observeStreak()
}

class ObserveAchievementsUseCase(private val repository: AchievementRepository) {
    operator fun invoke(): Flow<List<Achievement>> = repository.observeAchievements()
}

class UpdateStreakUseCase(
    private val repository: AchievementRepository
) {
    suspend operator fun invoke() {
        val currentStreakData = repository.getStreak()
        val now = Clock.System.now().toEpochMilliseconds()
        val today = PersianDateTime.parse(now)

        if (currentStreakData.lastTransactionDate == null) {
            // First transaction ever
            repository.updateStreak(
                Streak(
                    currentStreak = 1,
                    bestStreak = 1,
                    lastTransactionDate = now
                )
            )
            return
        }

        val lastDate = PersianDateTime.parse(currentStreakData.lastTransactionDate ?: 0L)

        if (today.year == lastDate.year && today.month == lastDate.month && today.day == lastDate.day) {
            // Already added transaction today, don't increment streak but update last time
            repository.updateStreak(currentStreakData.copy(lastTransactionDate = now))
            return
        }

        // Check if last transaction was "yesterday"
        // Simplest way: subtract one day from now and check if it matches lastDate
        val yesterday = PersianDateTime.parse(now - 86400000)

        if (yesterday.year == lastDate.year && yesterday.month == lastDate.month && yesterday.day == lastDate.day) {
            val newStreak = currentStreakData.currentStreak + 1
            repository.updateStreak(
                Streak(
                    currentStreak = newStreak,
                    bestStreak = max(newStreak, currentStreakData.bestStreak),
                    lastTransactionDate = now
                )
            )
        } else {
            // Streak broken
            repository.updateStreak(
                Streak(
                    currentStreak = 1,
                    bestStreak = currentStreakData.bestStreak,
                    lastTransactionDate = now
                )
            )
        }
    }
}

class CheckAchievementsUseCase(
    private val achievementRepository: AchievementRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val updateXP: UpdateXPUseCase
) {
    suspend operator fun invoke() {
        val streak = achievementRepository.getStreak()

        // 1. Initialize if empty
        achievementRepository.initializeAchievements(
            listOf(
                AchievementType.FIRST_TRANSACTION to 1,
                AchievementType.ONE_WEEK_STREAK to 7,
                AchievementType.FIRST_BUDGET to 1,
                AchievementType.SAVER to 1,
                AchievementType.HUNDRED_TRANSACTIONS to 100,
                AchievementType.NO_EXTRA_EXPENSE to 1,
                AchievementType.SIX_MONTHS_ACTIVE to 180,
                AchievementType.ZERO_DEBT to 1,
                AchievementType.BUDGET_MASTER to 5
            )
        )

        // 2. Check each
        checkFirstTransaction()
        checkStreakAchievements(streak.currentStreak)
        checkTransactionCount()
    }

    private suspend fun unlockWithXP(type: AchievementType) {
        val achievement = achievementRepository.getAchievement(type)
        if (achievement?.isUnlocked == false) {
            achievementRepository.unlockAchievement(type)
            updateXP(achievement.points)
        }
    }

    private suspend fun checkFirstTransaction() {
        unlockWithXP(AchievementType.FIRST_TRANSACTION)
    }

    private suspend fun checkStreakAchievements(currentStreak: Int) {
        val achievement = achievementRepository.getAchievement(AchievementType.ONE_WEEK_STREAK)
        if (achievement != null && !achievement.isUnlocked) {
            if (currentStreak >= achievement.target) {
                unlockWithXP(AchievementType.ONE_WEEK_STREAK)
            } else {
                achievementRepository.updateAchievementProgress(
                    AchievementType.ONE_WEEK_STREAK,
                    currentStreak,
                    false
                )
            }
        }
    }

    private suspend fun checkTransactionCount() {
        val count = transactionRepository.getTransactionCount()
        val achievement = achievementRepository.getAchievement(AchievementType.HUNDRED_TRANSACTIONS)
        if (achievement != null && !achievement.isUnlocked) {
            if (count >= achievement.target) {
                unlockWithXP(AchievementType.HUNDRED_TRANSACTIONS)
            } else {
                achievementRepository.updateAchievementProgress(
                    AchievementType.HUNDRED_TRANSACTIONS,
                    count.toInt(),
                    false
                )
            }
        }
    }
}
