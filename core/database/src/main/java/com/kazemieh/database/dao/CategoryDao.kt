package com.kazemieh.database.dao

import androidx.room.*
import com.kazemieh.database.entity.CategoryEntity

@Dao
interface CategoryDao {

    @Insert
    suspend fun insertCategory(category: CategoryEntity)

    @Query("SELECT * FROM category")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM category WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): CategoryEntity
}
