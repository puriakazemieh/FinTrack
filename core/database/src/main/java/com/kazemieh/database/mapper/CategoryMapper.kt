package com.kazemieh.database.mapper

import com.kazemieh.database.entity.CategoryEntity
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType

fun CategoryEntity.toCategory(): Category =
    Category(id, name, description, TransactionType.fromInt(type))

fun Category.toCategoryEntity(): CategoryEntity = CategoryEntity(
    name = name,
    description = description,
    type = type.count
)
