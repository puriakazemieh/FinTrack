package com.kazemieh.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kazemieh.database.entity.CategorySumEntity
import com.kazemieh.database.entity.TransactionEntity
import com.kazemieh.database.entity.TransactionPersonCrossRef
import com.kazemieh.database.entity.TransactionTagCrossRef
import com.kazemieh.database.entity.TransactionWithCategoryFinancialSourceAndTags
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransactionTagCrossRef(crossRef: TransactionTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransactionPersonCrossRef(crossRef: TransactionPersonCrossRef)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Transaction
    @Query("SELECT * FROM transactions")
    fun getAllTransactionsWithCategoryFinancialSourceAndTags(): PagingSource<Int, TransactionWithCategoryFinancialSourceAndTags>

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
                :tagIdsSize = 0 OR id IN (
                    SELECT transactionId
                    FROM transaction_tag
                    WHERE tagId IN (:tagIds)
                )
          )

          AND (
                :personIdsSize = 0 OR id IN (
                    SELECT transactionId
                    FROM transaction_person
                    WHERE personId IN (:personIds)
                )
          )

          AND (
              (:fromTimestamp IS NULL OR :toTimestamp IS NULL)
              OR (timeStamp >= :fromTimestamp AND timeStamp < :toTimestamp)
          )
    """
    )
    fun getAllTransactionsFiltered(
        type: Int? = null,
        categoryIds: List<Int> = emptyList(),
        sourceIds: List<Int> = emptyList(),
        tagIds: List<Int> = emptyList(),
        personIds: List<Int> = emptyList(),
        tagIdsSize: Int = tagIds.size,
        personIdsSize: Int = personIds.size,
        categoryIdsSize: Int = categoryIds.size,
        sourceIdsSize: Int = sourceIds.size,
        fromTimestamp: Long? = null,
        toTimestamp: Long? = null
    ): PagingSource<Int, TransactionWithCategoryFinancialSourceAndTags>

    @Query(
        """
    SELECT 
        c.id AS categoryId,
        c.name AS name,
        SUM(t.amount) AS totalAmount,
        c.type AS type
    FROM transactions t
    INNER JOIN category c ON t.categoryId = c.id
    
    WHERE (:type IS NULL OR t.type = :type)

      AND ((:categoryIdsSize = 0) OR t.categoryId IN (:categoryIds))

      AND ((:sourceIdsSize = 0) OR t.financialSourceId IN (:sourceIds))

      AND (
            :tagIdsSize = 0 OR t.id IN (
                SELECT transactionId
                FROM transaction_tag
                WHERE tagId IN (:tagIds)
            )
      )

      AND (
            :personIdsSize = 0 OR t.id IN (
                SELECT transactionId
                FROM transaction_person
                WHERE personId IN (:personIds)
            )
      )

      AND (
            (:fromTimestamp IS NULL OR :toTimestamp IS NULL)
            OR (t.timeStamp >= :fromTimestamp AND t.timeStamp < :toTimestamp)
      )

    GROUP BY c.id
    """
    )
    fun getCategorySums(
        type: Int?,
        categoryIds: List<Int>,
        sourceIds: List<Int>,
        tagIds: List<Int>,
        personIds: List<Int>,
        tagIdsSize: Int = tagIds.size,
        personIdsSize: Int = personIds.size,
        categoryIdsSize: Int = categoryIds.size,
        sourceIdsSize: Int = sourceIds.size,
        fromTimestamp: Long?,
        toTimestamp: Long?
    ): Flow<List<CategorySumEntity>>


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

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}
