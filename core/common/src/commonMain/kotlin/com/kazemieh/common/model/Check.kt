package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
data class Check(
    val id: Long = 0,
    val amount: Long,
    val date: Long,
    val dueDate: Long,
    val status: CheckStatus = CheckStatus.PENDING,
    val personId: Long,
    val personName: String? = null,
    val photoPath: String? = null,
    val description: String? = null,
    val isIncoming: Boolean = false // True if received from others, False if issued by me
)

enum class CheckStatus {
    PENDING,
    PASSED,
    REJECTED,
    CANCELLED
}
