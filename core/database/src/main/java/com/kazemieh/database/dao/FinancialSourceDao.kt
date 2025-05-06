package com.kazemieh.database.dao

import androidx.room.*
import com.kazemieh.database.entity.FinancialSourceEntity

@Dao
interface FinancialSourceDao {

    @Query("SELECT * FROM financial_source")
    suspend fun getAllSources(): List<FinancialSourceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: FinancialSourceEntity): Long

    @Delete
    suspend fun deleteSource(source: FinancialSourceEntity)
}
