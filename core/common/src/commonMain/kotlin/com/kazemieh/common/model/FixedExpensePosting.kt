package com.kazemieh.common.model

import kotlinx.serialization.Serializable

/** Immutable receipt for one posting attempt, including an explicitly confirmed repeat. */
@Serializable
data class FixedExpensePosting(
    val operationId: String,
    val expenseId: Long,
    val periodDueDate: Long,
    val transactionId: Long,
    val postedAt: Long,
    val isRepeat: Boolean = false
)
