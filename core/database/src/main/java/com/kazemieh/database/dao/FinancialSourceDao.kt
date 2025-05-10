package com.kazemieh.database.dao

import androidx.room.*
import com.kazemieh.database.entity.FinancialSourceEntity

@Dao
interface FinancialSourceDao {

    @Insert
    suspend fun insertFinancialSource(financialSource: FinancialSourceEntity)

    @Query("SELECT * FROM financial_source")
    suspend fun getAllFinancialSources(): List<FinancialSourceEntity>

    @Query("SELECT * FROM financial_source WHERE id = :financialSourceId")
    suspend fun getFinancialSourceById(financialSourceId: Long): FinancialSourceEntity
}

