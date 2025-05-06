package com.kazemieh.database.dao

import androidx.room.*
import com.kazemieh.database.entity.TagEntity

@Dao
interface TagDao {

    @Query("SELECT * FROM tag")
    suspend fun getAllTags(): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Delete
    suspend fun deleteTag(tag: TagEntity)
}
