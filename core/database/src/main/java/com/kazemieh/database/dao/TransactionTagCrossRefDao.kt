package com.kazemieh.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kazemieh.database.entity.TransactionTagCrossRef

@Dao
interface TransactionTagCrossRefDao {

    @Insert
    suspend fun insertTransactionTagCrossRef(transactionTagCrossRef: TransactionTagCrossRef)

    @Query("SELECT * FROM transaction_tag WHERE transactionId = :transactionId")
    suspend fun getTagsForTransaction(transactionId: Long): List<TransactionTagCrossRef>

    @Query("SELECT * FROM transaction_tag WHERE tagId = :tagId")
    suspend fun getTransactionsForTag(tagId: Long): List<TransactionTagCrossRef>
}
