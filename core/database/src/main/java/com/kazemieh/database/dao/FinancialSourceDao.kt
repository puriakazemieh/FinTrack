package com.kazemieh.database.dao

import androidx.room.*
import com.kazemieh.database.entity.CategoryEntity
import com.kazemieh.database.entity.SourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialSourceDao {

    @Insert
    suspend fun insertFinancialSource(financialSource: SourceEntity): Long

    @Query("SELECT * FROM source")
    fun getAllFinancialSources(): Flow<List<SourceEntity>>

    @Query("SELECT * FROM source WHERE id = :id")
    fun getFinancialSourceById(id: Long): Flow<SourceEntity?>

    @Query("UPDATE source SET balance = balance + :amount WHERE id = :id")
    suspend fun increaseBalance(id: Long, amount: Int)

    @Query("UPDATE source SET balance = balance - :amount WHERE id = :id")
    suspend fun decreaseBalance(id: Long, amount: Int)

    @Query("SELECT * FROM source WHERE id = 1 LIMIT 1")
    suspend fun getDefaultSource(): SourceEntity?

    @Delete
    suspend fun deleteSource(source: SourceEntity)

    @Update
    suspend fun updateSource(source: SourceEntity): Int
}

