package com.kazemieh.gamification.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Achievement
import com.kazemieh.common.model.Streak
import com.kazemieh.domain.usecase.ObserveAchievementsUseCase
import com.kazemieh.domain.usecase.ObserveStreakUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class GamificationViewModel(
    observeStreak: ObserveStreakUseCase,
    observeAchievements: ObserveAchievementsUseCase
) : ViewModel() {

    val state: StateFlow<GamificationState> = combine(
        observeStreak(),
        observeAchievements()
    ) { streak, achievements ->
        GamificationState(
            streak = streak,
            achievements = achievements
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GamificationState()
    )
}

data class GamificationState(
    val streak: Streak = Streak(),
    val achievements: List<Achievement> = emptyList()
)
