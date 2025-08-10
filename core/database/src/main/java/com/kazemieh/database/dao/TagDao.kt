package com.kazemieh.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kazemieh.database.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert
    suspend fun insertTag(tag: TagEntity)

    @Query("SELECT * FROM tag")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tag WHERE id = :tagId")
    suspend fun getTagById(tagId: Long): TagEntity
}

