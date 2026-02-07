package com.kazemieh.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kazemieh.common.model.TransactionType
import kotlinx.serialization.Serializable

@Entity(tableName = "category")
@Serializable
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val type: Int,
    val colorId: Int = 1,
    val iconId: Int = 1
)

@Serializable
data class CategorySumEntity(
    val categoryId: Long,
    val name: String,
    val totalAmount: Long,
    val type: TransactionType
)
