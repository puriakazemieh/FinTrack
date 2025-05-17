package com.kazemieh.data.mapper

import com.kazemieh.database.entity.CategoryEntity
import com.kazemieh.model.Category

fun CategoryEntity.toCategory(): Category = Category(id, name)
fun Category.toCategoryEntity(): CategoryEntity = CategoryEntity(id, name)
