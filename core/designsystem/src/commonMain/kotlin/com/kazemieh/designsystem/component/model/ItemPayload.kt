package com.kazemieh.designsystem.component.model

import com.kazemieh.common.model.TransactionType

sealed interface ItemPayload {
    data class TagPayload(val id: Long, val name: String) : ItemPayload
    data class PersonPayload(val id: Long, val name: String) : ItemPayload
    data class CategoryPayload(val id: Long, val name: String, val type: TransactionType) : ItemPayload
    data class SourcePayload(val id: Long, val name: String, val formattedBalance: String) : ItemPayload
}