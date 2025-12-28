package com.kazemieh.database.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TransactionWithCategoryFinancialSourceAndTags(
    @Embedded val transaction: TransactionEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity,

    @Relation(
        parentColumn = "sourceId",
        entityColumn = "id"
    )
    val financialSource: SourceEntity,

    @Relation(
        parentColumn = "sourceEndId",
        entityColumn = "id"
    )
    val sourceEnd: SourceEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TransactionTagCrossRef::class,
            parentColumn = "transactionId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TransactionPersonCrossRef::class,
            parentColumn = "transactionId",
            entityColumn = "personId"
        )
    )
    val persons: List<PersonEntity>
)
