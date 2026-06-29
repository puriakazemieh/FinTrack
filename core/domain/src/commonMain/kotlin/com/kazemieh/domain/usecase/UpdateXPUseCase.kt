package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.AchievementRepository
import kotlin.math.max

class UpdateXPUseCase(
    private val repository: AchievementRepository
) {
    suspend operator fun invoke(amount: Int) {
        val streak = repository.getStreak()
        val newXp = streak.xp + amount
        val newLevel = (newXp / 1000) + 1
        
        repository.updateStreak(
            streak.copy(
                xp = newXp,
                level = max(streak.level, newLevel)
            )
        )
    }
}
