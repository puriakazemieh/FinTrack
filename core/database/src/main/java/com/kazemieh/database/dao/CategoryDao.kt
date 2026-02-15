package com.kazemieh.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kazemieh.common.model.TransactionType
import com.kazemieh.database.entity.CategoryEntity
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

    @Insert
    suspend fun createTransferCategory(
        category: CategoryEntity = CategoryEntity(
            name = "انتقال",
            type = TransactionType.TRANSFER.count
        )
    ): Long

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT * FROM category WHERE type = :type ORDER BY id LIMIT 1")
    suspend fun getDefaultCategory(type: Int): CategoryEntity

    @Query("SELECT * FROM category WHERE type = 3 ORDER BY id LIMIT 1")
    suspend fun getTransferCategory(): CategoryEntity?


}
