package com.kazemieh.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kazemieh.database.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert
    suspend fun insertTag(tag: TagEntity): Long

    @Insert
    suspend fun insertAllTag(tag: List<TagEntity>): List<Long>

    @Query("SELECT * FROM tag")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tag WHERE id = :tagId")
    suspend fun getTagById(tagId: Long): TagEntity

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Update
    suspend fun updateTag(tag: TagEntity): Int
}

