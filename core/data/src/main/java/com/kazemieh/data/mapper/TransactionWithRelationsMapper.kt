package com.kazemieh.data.mapper

import com.kazemieh.database.entity.TransactionWithCategoryFinancialSourceAndTags
import com.kazemieh.model.TransactionWithRelations

fun TransactionWithCategoryFinancialSourceAndTags.toTransactionWithRelations(): TransactionWithRelations =
    TransactionWithRelations(
        transaction = transaction.toTransaction(),
        category = category.toCategory(),
        financialSource = financialSource.toFinancialSource(),
        tags = tags.map { it.toTag() }
    )
