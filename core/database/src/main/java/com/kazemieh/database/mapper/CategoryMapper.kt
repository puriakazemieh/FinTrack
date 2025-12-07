package com.kazemieh.database.mapper

import com.kazemieh.common.model.Category
import com.kazemieh.common.model.CategorySum
import com.kazemieh.common.model.TransactionType
import com.kazemieh.database.entity.CategoryEntity
import com.kazemieh.database.entity.CategorySumEntity

fun CategoryEntity.toCategory(): Category =
    Category(id, name, description, TransactionType.fromInt(type))

fun Category.toCategoryEntity(): CategoryEntity = CategoryEntity(
    name = name,
    description = description,
    type = type.count
)

fun CategorySumEntity.toCategory(): CategorySum =
    CategorySum(
        categoryId = categoryId,
        name = name,
        totalAmount = totalAmount,
        type =type
    )

fun CategorySum.toCategoryEntity(): CategorySumEntity = CategorySumEntity(
    categoryId = categoryId,
    name = name,
    totalAmount = totalAmount,
    type =type
)
