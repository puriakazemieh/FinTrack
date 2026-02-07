package com.kazemieh.database.mapper

import com.kazemieh.common.model.Category
import com.kazemieh.common.model.CategorySum
import com.kazemieh.common.model.TransactionType
import com.kazemieh.database.entity.CategoryEntity
import com.kazemieh.database.entity.CategorySumEntity

fun CategoryEntity.toCategory(): Category =
    Category(
        id,
        name,
        description,
        TransactionType.fromInt(type),
        colorId = colorId,
        iconId = iconId
    )

fun Category.toCategoryEntity(): CategoryEntity = CategoryEntity(
    id = id ?: 0,
    name = name,
    description = description,
    type = type.count,
    colorId = colorId,
    iconId = iconId
)

fun CategorySumEntity.toCategory(): CategorySum =
    CategorySum(
        categoryId = categoryId,
        name = name,
        totalAmount = totalAmount,
        type = type
    )
