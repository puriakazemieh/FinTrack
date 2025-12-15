package com.kazemieh.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "source")
@Serializable
data class SourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val balance: Int = 0,
    val cardNumber: String? = null,
    val description: String? = null,
    val type: Int,
)
