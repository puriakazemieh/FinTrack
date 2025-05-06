package com.kazemieh.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "transaction",
    foreignKeys = [
        ForeignKey(entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = FinancialSourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["financialSourceId"],
            onDelete = ForeignKey.CASCADE)
    ])
@Serializable
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val categoryId: Long,
    val financialSourceId: Long,
    val description: String?,
    val tags: String,
    val date: String
)
