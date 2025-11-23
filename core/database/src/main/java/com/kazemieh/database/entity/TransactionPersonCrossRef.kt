package com.kazemieh.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

@Entity(
    tableName = "transaction_person",
    primaryKeys = ["transactionId", "personId"],
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("transactionId"), Index("personId")]
)
data class TransactionPersonCrossRef(
    val transactionId: Long,
    val personId: Long,
)
