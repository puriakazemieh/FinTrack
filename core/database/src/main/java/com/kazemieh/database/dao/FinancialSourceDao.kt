package com.kazemieh.database.dao

import androidx.room.*
import com.kazemieh.database.entity.FinancialSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialSourceDao {

    @Insert
    suspend fun insertFinancialSource(financialSource: FinancialSourceEntity): Long

    @Query("SELECT * FROM financial_source")
    fun getAllFinancialSources(): Flow<List<FinancialSourceEntity>>

    @Query("SELECT * FROM financial_source WHERE id = :financialSourceId")
    suspend fun getFinancialSourceById(financialSourceId: Long): FinancialSourceEntity

    @Query("UPDATE financial_source SET balance = balance + :amount WHERE id = :id")
    suspend fun increaseBalance(id: Long, amount: Int)

    @Query("UPDATE financial_source SET balance = balance - :amount WHERE id = :id")
    suspend fun decreaseBalance(id: Long, amount: Int)

    @Query("SELECT * FROM financial_source WHERE id = 0 LIMIT 1")
    suspend fun getDefaultSource(): FinancialSourceEntity
}

