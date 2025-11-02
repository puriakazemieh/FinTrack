package com.kazemieh.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kazemieh.database.entity.TransactionEntity
import com.kazemieh.database.entity.TransactionTagCrossRef
import com.kazemieh.database.entity.TransactionWithCategoryFinancialSourceAndTags
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransactionTagCrossRef(crossRef: TransactionTagCrossRef)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Transaction
    @Query("SELECT * FROM transactions")
    fun getAllTransactionsWithCategoryFinancialSourceAndTags(): Flow<List<TransactionWithCategoryFinancialSourceAndTags>>

    @Transaction
    @Query("SELECT * FROM transactions  WHERE type = :type")
    fun getAllTransactionsByTypeWithCategoryFinancialSourceAndTags(type: Int): Flow<List<TransactionWithCategoryFinancialSourceAndTags>>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE (:type IS NULL OR type = :type)
          AND ((:categoryIdsSize = 0) OR categoryId IN (:categoryIds))
          AND ((:sourceIdsSize = 0) OR financialSourceId IN (:sourceIds))
          AND (
              (:fromTimestamp IS NULL OR :toTimestamp IS NULL)
              OR (timeStamp BETWEEN :fromTimestamp AND :toTimestamp)
          )
    """
    )
    fun getAllTransactionsFiltered(
        type: Int? = null,
        categoryIds: List<Int> = emptyList(),
        sourceIds: List<Int> = emptyList(),
        categoryIdsSize: Int = categoryIds.size,
        sourceIdsSize: Int = categoryIds.size,
        fromTimestamp: Long? = null,
        toTimestamp: Long? = null
    ): Flow<List<TransactionWithCategoryFinancialSourceAndTags>>


    @Transaction
    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId")
    suspend fun getTransactionsByCategoryId(categoryId: Long): List<TransactionWithCategoryFinancialSourceAndTags>

    @Transaction
    @Query("SELECT * FROM transactions WHERE financialSourceId = :financialSourceId")
    suspend fun getTransactionsByFinancialSourceId(financialSourceId: Long): List<TransactionWithCategoryFinancialSourceAndTags>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        INNER JOIN transaction_tag ON transactions.id = transaction_tag.transactionId
        INNER JOIN tag ON transaction_tag.tagId = tag.id
        WHERE tag.name = :tagName
    """
    )
    suspend fun getTransactionsByTag(tagName: String): List<TransactionWithCategoryFinancialSourceAndTags>

//    @Transaction
//    @Query(
//        """
//    SELECT * FROM transactions WHERE id IN (
//        SELECT transactionId FROM transaction_tag
//        INNER JOIN tag ON transaction_tag.tagId = tag.id
//        WHERE tag.name = :tagName
//    )
//    """
//    )
//    suspend fun getTransactionsByTag(tagName: String): List<TransactionWithCategoryFinancialSourceAndTags>

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}
