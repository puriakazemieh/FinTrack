package com.kazemieh.database.mapper

import com.kazemieh.database.entity.TransactionWithCategoryFinancialSourceAndTags
import com.kazemieh.common.model.TransactionWithRelations

fun TransactionWithCategoryFinancialSourceAndTags.toTransactionWithRelations(): TransactionWithRelations =
    TransactionWithRelations(
        transaction = transaction.toTransaction(),
        categoryName = category.name,
        financialSourceName = financialSource.name,
        tags = tags.joinToString { it.name },
        persons = persons.joinToString { it.name }
    )
