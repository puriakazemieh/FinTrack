package com.kazemieh.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Transaction
import com.kazemieh.database.entity.TransactionEntity
import com.kazemieh.database.entity.TransactionWithCategoryFinancialSourceAndTags
import com.kazemieh.database.entity.TransactionWithTags

@Dao
interface TransactionDao {

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Transaction
    @Query("SELECT * FROM 'transaction' WHERE id = :id")
    suspend fun getTransactionWithCategoryAndFinancialSource(id: Long): TransactionWithCategoryFinancialSourceAndTags?

    @Transaction
    @Query("SELECT * FROM 'transaction'")
    suspend fun getAllTransactionsWithCategoryAndFinancialSource(): List<TransactionWithCategoryFinancialSourceAndTags>

    @Transaction
    @Query("SELECT * FROM 'transaction' WHERE id = :id")
    suspend fun getTransactionWithTags(id: Long): TransactionWithTags?
}
