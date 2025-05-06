package com.kazemieh.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithTags(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val tags: List<TagEntity>
)
