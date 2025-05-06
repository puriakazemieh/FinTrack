package com.kazemieh.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_tag")
data class TransactionTagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,  // Foreign Key to Transaction
    val tagId: Long  // Foreign Key to Tag
)
