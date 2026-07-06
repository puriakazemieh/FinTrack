package com.kazemieh.domain.usecase

import com.kazemieh.common.model.SyncStatus
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.repository.TransactionRepository
import kotlin.math.abs

data class DetectedSubscription(
    val categoryId: Long,
    val categoryName: String,
    val amount: Long,
    val averageIntervalDays: Int,
    val lastPaymentTimestamp: Long,
    val nextEstimatedTimestamp: Long,
    val occurrences: Int
)

/**
 * Detects likely recurring payments (subscriptions) by looking for expenses with the
 * same category and amount that repeat on a roughly monthly cadence. This reads only
 * real transaction history — there is no seeded/mock data, so an empty result is a
 * legitimate outcome for a new or low-history account.
 */
class DetectSubscriptionsUseCase(
    private val repository: TransactionRepository
) {
    companion object {
        private const val MIN_OCCURRENCES = 2
        private const val MIN_INTERVAL_DAYS = 20
        private const val MAX_INTERVAL_DAYS = 40
        private const val MAX_INTERVAL_DEVIATION_DAYS = 10
        private const val DAY_MS = 24L * 60 * 60 * 1000
    }

    suspend operator fun invoke(): List<DetectedSubscription> {
        val transactions = repository.getAllTransactions()
            .filter { it.syncStatus != SyncStatus.DELETED && it.type == TransactionType.EXPENSE }

        return transactions
            .groupBy { it.categoryId to it.amount }
            .mapNotNull { (key, group) ->
                if (group.size < MIN_OCCURRENCES) return@mapNotNull null
                val sorted = group.sortedBy { it.timeStamp }
                val gapsInDays = sorted.zipWithNext { a, b -> (b.timeStamp - a.timeStamp) / DAY_MS }
                val avgGap = gapsInDays.average()
                if (avgGap < MIN_INTERVAL_DAYS || avgGap > MAX_INTERVAL_DAYS) return@mapNotNull null
                val maxDeviation = gapsInDays.maxOf { abs(it - avgGap) }
                if (maxDeviation > MAX_INTERVAL_DEVIATION_DAYS) return@mapNotNull null

                val (categoryId, amount) = key
                val category = repository.getCategoryById(categoryId)
                val lastTimestamp = sorted.last().timeStamp
                DetectedSubscription(
                    categoryId = categoryId,
                    categoryName = category?.name ?: "",
                    amount = amount.toLong(),
                    averageIntervalDays = avgGap.toInt(),
                    lastPaymentTimestamp = lastTimestamp,
                    nextEstimatedTimestamp = lastTimestamp + (avgGap.toLong() * DAY_MS),
                    occurrences = sorted.size
                )
            }
            .sortedByDescending { it.occurrences }
    }
}
