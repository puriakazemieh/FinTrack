package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
data class SmsDraft(
    val id: Long = 0,
    val sender: String,
    val body: String,
    val amount: Int,
    val bankName: String,
    val type: TransactionType,
    val sourceId: Long? = null,
    val sourceIdentifier: String? = null,
    val timeStamp: Long,
    val date: String,
    val isUsed: Boolean = false
)
