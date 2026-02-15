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

//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    suspend fun insertTransactionTagCrossRef(crossRef: TransactionTagCrossRef)

//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    suspend fun insertTransactionPersonCrossRef(crossRef: TransactionPersonCrossRef)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity): Int

//    @Update
//    suspend fun updateTransactionTagCrossRef(crossRef: TransactionTagCrossRef)
//
//    @Update
//    suspend fun updateTransactionPersonCrossRef(crossRef: TransactionPersonCrossRef)

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE (:type IS NULL OR type = :type)
          AND ((:categoryIdsSize = 0) OR categoryId IN (:categoryIds))
          AND ((:sourceIdsSize = 0) OR sourceId IN (:sourceIds))

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
        categoryIds: List<Long?> = emptyList(),
        sourceIds: List<Long?> = emptyList(),
        tagIds: List<Long?> = emptyList(),
        personIds: List<Long?> = emptyList(),
        tagIdsSize: Int = tagIds.size,
        personIdsSize: Int = personIds.size,
        categoryIdsSize: Int = categoryIds.size,
        sourceIdsSize: Int = sourceIds.size,
        fromTimestamp: Long? = null,
        toTimestamp: Long? = null
    ): PagingSource<Int, TransactionWithCategoryFinancialSourceAndTags>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE (:type IS NULL OR type = :type)
          AND ((:categoryIdsSize = 0) OR categoryId IN (:categoryIds))
          AND ((:sourceIdsSize = 0) OR sourceId IN (:sourceIds))

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
    suspend fun getAllTransactionListFiltered(
        type: Int? = null,
        categoryIds: List<Long?> = emptyList(),
        sourceIds: List<Long?> = emptyList(),
        tagIds: List<Long?> = emptyList(),
        personIds: List<Long?> = emptyList(),
        tagIdsSize: Int = tagIds.size,
        personIdsSize: Int = personIds.size,
        categoryIdsSize: Int = categoryIds.size,
        sourceIdsSize: Int = sourceIds.size,
        fromTimestamp: Long? = null,
        toTimestamp: Long? = null
    ): List<TransactionWithCategoryFinancialSourceAndTags>

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

      AND ((:sourceIdsSize = 0) OR t.sourceId IN (:sourceIds))

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
        categoryIds: List<Long?>,
        sourceIds: List<Long?>,
        tagIds: List<Long?>,
        personIds: List<Long?>,
        tagIdsSize: Int = tagIds.size,
        personIdsSize: Int = personIds.size,
        categoryIdsSize: Int = categoryIds.size,
        sourceIdsSize: Int = sourceIds.size,
        fromTimestamp: Long?,
        toTimestamp: Long?
    ): Flow<List<CategorySumEntity>>


    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransactionTagCrossRefs(crossRefs: List<TransactionTagCrossRef>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransactionPersonCrossRefs(crossRefs: List<TransactionPersonCrossRef>)

    @Query("DELETE FROM transaction_tag WHERE transactionId = :transactionId")
    suspend fun deleteAllTagRefsForTransaction(transactionId: Long)

    @Query("DELETE FROM transaction_person WHERE transactionId = :transactionId")
    suspend fun deleteAllPersonRefsForTransaction(transactionId: Long)

    @Query("UPDATE transactions SET categoryId = :toCategoryId WHERE categoryId = :fromCategoryId")
    suspend fun moveTransactionsCategory(fromCategoryId: Long, toCategoryId: Long)

    @Query("UPDATE transactions SET sourceId = :toSourceId WHERE sourceId = :fromSourceId")
    suspend fun moveTransactionsSource(fromSourceId: Long, toSourceId: Long)

    // خیلی مهم: برای transfer مقصد هم باید جابجا بشه
    @Query("UPDATE transactions SET sourceEndId = :toSourceId WHERE sourceEndId = :fromSourceId")
    suspend fun moveTransactionsSourceEnd(fromSourceId: Long, toSourceId: Long)

    // اگر بدون move حذف می‌کنی (برای جلوگیری از orphan)
    @Query("UPDATE transactions SET sourceEndId = NULL WHERE sourceEndId = :fromSourceId")
    suspend fun nullifySourceEnd(fromSourceId: Long)

    @Query(
        """
    INSERT OR IGNORE INTO transaction_tag(transactionId, tagId)
    SELECT transactionId, :toTagId
    FROM transaction_tag
    WHERE tagId = :fromTagId
"""
    )
    suspend fun copyTagRefs(fromTagId: Long, toTagId: Long)

    @Query("DELETE FROM transaction_tag WHERE tagId = :tagId")
    suspend fun deleteTagRefs(tagId: Long)

    @Query(
        """
    INSERT OR IGNORE INTO transaction_person(transactionId, personId)
    SELECT transactionId, :toPersonId
    FROM transaction_person
    WHERE personId = :fromPersonId
"""
    )
    suspend fun copyPersonRefs(fromPersonId: Long, toPersonId: Long)

    @Query("DELETE FROM transaction_person WHERE personId = :personId")
    suspend fun deletePersonRefs(personId: Long)


}
