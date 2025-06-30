package com.kazemieh.database.mapper

import com.kazemieh.database.entity.FinancialSourceEntity
import com.kazemieh.model.FinancialSource

fun FinancialSourceEntity.toFinancialSource(): FinancialSource = FinancialSource(id, name)
fun FinancialSource.toFinancialSourceEntity(): FinancialSourceEntity = FinancialSourceEntity(id, name)
