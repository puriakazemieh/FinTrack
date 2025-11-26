package com.kazemieh.database.mapper

import com.kazemieh.database.entity.TagEntity
import com.kazemieh.common.model.Tag

fun TagEntity.toTag(): Tag = Tag(id, name, description)
fun Tag.toTagEntity(): TagEntity = TagEntity(name = name, description = description)
