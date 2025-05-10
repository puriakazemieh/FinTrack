package com.kazemieh.database.dao

import androidx.room.*
import com.kazemieh.database.entity.TagEntity

@Dao
interface TagDao {

    @Insert
    suspend fun insertTag(tag: TagEntity)

    @Query("SELECT * FROM tag")
    suspend fun getAllTags(): List<TagEntity>

    @Query("SELECT * FROM tag WHERE id = :tagId")
    suspend fun getTagById(tagId: Long): TagEntity
}

