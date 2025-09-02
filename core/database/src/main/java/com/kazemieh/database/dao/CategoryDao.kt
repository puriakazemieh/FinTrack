package com.kazemieh.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kazemieh.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert
    suspend fun insertCategory(category: CategoryEntity): Long

    @Query("SELECT * FROM category")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM category WHERE id = :categoryId")
    fun getCategoryById(categoryId: Long): Flow<CategoryEntity>
}
