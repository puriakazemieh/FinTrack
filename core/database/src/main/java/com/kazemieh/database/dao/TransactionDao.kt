package com.kazemieh.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Transaction
import com.kazemieh.database.entity.TransactionEntity
import com.kazemieh.database.entity.TransactionWithCategoryFinancialSourceAndTags

@Dao
interface TransactionDao {

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Transaction
    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsWithCategoryFinancialSourceAndTags(): List<TransactionWithCategoryFinancialSourceAndTags>

    @Transaction
    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId")
    suspend fun getTransactionsByCategoryId(categoryId: Long): List<TransactionWithCategoryFinancialSourceAndTags>

    @Transaction
    @Query("SELECT * FROM transactions WHERE financialSourceId = :financialSourceId")
    suspend fun getTransactionsByFinancialSourceId(financialSourceId: Long): List<TransactionWithCategoryFinancialSourceAndTags>

    @Transaction
    @Query("""
        SELECT * FROM transactions
        INNER JOIN transaction_tag ON transactions.id = transaction_tag.transactionId
        INNER JOIN tag ON transaction_tag.tagId = tag.id
        WHERE tag.name = :tagName
    """)
    suspend fun getTransactionsByTag(tagName: String): List<TransactionWithCategoryFinancialSourceAndTags>

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}
