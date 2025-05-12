package com.kazemieh.model

import kotlinx.serialization.Serializable

@Serializable
data class TransactionWithRelations(
    val transaction: Transaction,
    val category: Category,
    val financialSource: FinancialSource,
    val tags: List<Tag>
)