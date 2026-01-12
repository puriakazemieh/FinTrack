package com.kazemieh.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kazemieh.common.model.TransactionType
import com.kazemieh.database.entity.CategoryEntity
import com.kazemieh.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert
    suspend fun insertAllCategory(category: List<CategoryEntity>): List<Long>

    @Update
    suspend fun updateCategory(category: CategoryEntity): Int

    @Query("SELECT * FROM category WHERE type = :type")
    fun getAllCategories(type: Int): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM category WHERE id = :categoryId")
    fun getCategoryById(categoryId: Long): Flow<CategoryEntity>

    @Query("SELECT * FROM category WHERE type = :type AND id = 1 LIMIT 1")
    suspend fun getDefaultCategory(type: Int): CategoryEntity

    @Query("SELECT * FROM category WHERE type = 3 LIMIT 1")
    suspend fun getTransferCategory(): CategoryEntity

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}
