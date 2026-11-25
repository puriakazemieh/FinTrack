package com.kazemieh.common.model

/** The snapshot the user/worker selected; storage revalidates it before any write. */
data class FixedExpensePostingRequest(
    val operationId: String,
    val expense: FixedExpense,
    val periodDueDate: Long,
    val nextDueDate: Long,
    val nextIsActive: Boolean,
    val isRepeat: Boolean = false
)
