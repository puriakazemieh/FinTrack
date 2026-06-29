package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
data class Achievement(
    val id: Long,
    val type: AchievementType,
    val isUnlocked: Boolean = false,
    val unlockTime: Long? = null,
    val progress: Int = 0,
    val target: Int = 1,
    val points: Int = 100 // XP awarded
)

@Serializable
enum class AchievementType(val key: String) {
    FIRST_TRANSACTION("first_transaction"),
    ONE_WEEK_STREAK("one_week_streak"),
    FIRST_BUDGET("first_budget"),
    SAVER("saver"),
    HUNDRED_TRANSACTIONS("hundred_transactions"),
    NO_EXTRA_EXPENSE("no_extra_expense"),
    SIX_MONTHS_ACTIVE("six_months_active"),
    ZERO_DEBT("zero_debt"),
    BUDGET_MASTER("budget_master")
}

@Serializable
data class Streak(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastTransactionDate: Long? = null,
    val xp: Int = 0,
    val level: Int = 1
) {
    val nextLevelXp: Int get() = level * 1000
    val progress: Float get() = (xp % 1000).toFloat() / 1000f
}
