package com.kazemieh.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "financial_source")
@Serializable
data class FinancialSourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)
