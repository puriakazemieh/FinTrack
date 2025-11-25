package com.kazemieh.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FinancialSourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["financialSourceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Serializable
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Int,
    val amountTransfer: Int? = null,
    val categoryId: Long,
    val financialSourceId: Long,
    val financialSourceEndId: Long? = null,
    val description: String?,
    val timeStamp: Long,
    val type: Int,
)
