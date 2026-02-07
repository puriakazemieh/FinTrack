package com.kazemieh.database.mapper

import com.kazemieh.common.model.Tag
import com.kazemieh.database.entity.TagEntity

fun TagEntity.toTag(): Tag = Tag(
    id = id,
    name = name,
    description = description,
    colorId = colorId,
    iconId = iconId
)

fun Tag.toTagEntity(): TagEntity = TagEntity(
    id = id ?: 0,
    name = name,
    description = description,
    colorId = colorId,
    iconId = iconId
)
