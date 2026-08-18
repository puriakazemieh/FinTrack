package com.kazemieh.common.model

import kotlinx.serialization.Serializable

@Serializable
data class SmsDraft(
    val id: Long = 0,
    val sender: String,
    val body: String,
    val amount: Long,
    val bankName: String,
    val type: TransactionType,
    val categoryId: Long? = null,
    val sourceId: Long? = null,
    val sourceIdentifier: String? = null,
    val timeStamp: Long,
    val date: String,
    val confidence: Int = 100,
    val isUsed: Boolean = false
)
