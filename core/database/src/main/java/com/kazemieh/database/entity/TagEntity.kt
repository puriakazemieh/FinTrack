package com.kazemieh.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "tag")
@Serializable
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = 0,
    val name: String
)

