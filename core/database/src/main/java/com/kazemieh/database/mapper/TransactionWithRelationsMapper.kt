package com.kazemieh.database.mapper

import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.database.entity.TransactionWithCategoryFinancialSourceAndTags

fun TransactionWithCategoryFinancialSourceAndTags.toTransactionWithRelations(): TransactionWithRelations =
    TransactionWithRelations(
        transaction = transaction.toTransaction(),
        category = category.toCategory(),
        source = financialSource.toSource(),
        sourceEnd = sourceEnd?.toSource(),
        tags = tags.map { it.toTag() },
        persons = persons.map { it.toPerson() }
    )
