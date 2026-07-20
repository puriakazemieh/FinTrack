package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
enum class GoalType {
    SAVINGS,        // پس‌انداز عمومی
    EMERGENCY_FUND, // صندوق اضطراری (مرحله ۳ استقلال مالی)
    DEBT_PAYOFF,    // تسویه بدهی (مرحله ۴ استقلال مالی)
    RETIREMENT,     // بازنشستگی / امنیت مالی (مرحله ۵ و ۶)
    BIG_PURCHASE,   // خرید بزرگ (خودرو، مسکن)
    INVESTMENT      // سرمایه‌گذاری / سبد دارایی
}

@Serializable
enum class GoalCategory {
    SHORT_TERM, // < 1 year
    MID_TERM,   // 1-5 years
    LONG_TERM   // > 5 years
}

@Serializable
enum class GoalPriority {
    LOW,
    MEDIUM,
    HIGH
}

@Serializable
enum class FreedomStage(val level: Int) {
    DEPENDENCE(1),
    SOLVENCY(2),
    STABILITY(3),
    DEBT_FREEDOM(4),
    SECURITY(5),
    INDEPENDENCE(6),
    ABUNDANCE(7)
}

@Serializable
data class GoalTemplate(
    val id: Long = 0,
    val name: String,
    val systemType: GoalType = GoalType.SAVINGS,
    val iconId: Int = 1,
    val colorId: Int = 1,
    override val updatedAt: Long = 0,
    override val syncStatus: SyncStatus = SyncStatus.SYNCED
) : SyncableEntity

@Serializable
data class GoalBasket(
    val id: Long = 0,
    val name: String,
    val iconId: Int = 1,
    val colorId: Int = 1,
    override val updatedAt: Long = 0,
    override val syncStatus: SyncStatus = SyncStatus.SYNCED
) : SyncableEntity

@Serializable
data class Goal(
    val id: Long = 0,
    val name: String,
    val targetAmount: Long,
    val savedAmount: Long = 0,
    val iconId: Int = 1,
    val colorId: Int = 1,
    val startDate: Long = 0,
    val endDate: Long? = null,
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val monthlyTarget: Long = 0,
    val type: GoalType = GoalType.SAVINGS,
    val category: GoalCategory = GoalCategory.SHORT_TERM,
    val priority: GoalPriority = GoalPriority.MEDIUM,
    val description: String = "",
    val templateId: Long? = null,
    val basketId: Long? = null,
    override val updatedAt: Long = 0,
    override val syncStatus: SyncStatus = SyncStatus.SYNCED
) : SyncableEntity {
    val progress: Float
        get() = if (targetAmount > 0) savedAmount.toFloat() / targetAmount.toFloat() else 0f
    
    val percent: Int
        get() = (progress * 100).toInt()
}
