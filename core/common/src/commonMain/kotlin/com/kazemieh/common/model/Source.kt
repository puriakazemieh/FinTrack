package com.kazemieh.common.model

import com.kazemieh.common.formatted
import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class Source(
    val id: Long? = null,
    val name: String,
    val balance: Int = 0,
    val cardNumber: String? = null,
    val description: String? = null,
    val type: Int = 0,
    val formattedBalance: String = balance.formatted(),
    val colorId: Int,
    val iconId: Int,
    val shabaNumber: String? = null,
    val accountNumber: String? = null,
    val cvv2: String? = null,
    val expirationMonth: String? = null,
    val expirationYear: String? = null,
    val branchCode: String? = null,
    val branchName: String? = null
)
