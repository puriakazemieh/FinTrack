package com.kazemieh.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithCategoryFinancialSourceAndTags(
    @Embedded val transaction: TransactionEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity,

    @Relation(
        parentColumn = "financialSourceId",
        entityColumn = "id"
    )
    val financialSource: FinancialSourceEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val tags: List<TagEntity>
)
