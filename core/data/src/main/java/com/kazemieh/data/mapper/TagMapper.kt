package com.kazemieh.data.mapper

import com.kazemieh.database.entity.TagEntity
import com.kazemieh.model.Tag

fun TagEntity.toTag(): Tag = Tag(id, name)
fun Tag.toTagEntity(): TagEntity = TagEntity(id, name)
