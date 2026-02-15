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

    @Query("UPDATE source SET balance = balance + :delta WHERE id = :id")
    suspend fun adjustBalance(id: Long, delta: Int)

    @Delete
    suspend fun deleteSource(source: SourceEntity)

    @Update
    suspend fun updateSource(source: SourceEntity): Int

    @Query("SELECT * FROM source ORDER BY id LIMIT 1")
    suspend fun getDefaultSource(): SourceEntity?

}

