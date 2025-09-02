package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.model.TransactionWithRelations
import kotlinx.serialization.json.Json

class ImportTransactionsUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(json: String) {
        val transactions = Json.decodeFromString<List<TransactionWithRelations>>(json)
        transactions.forEach { t ->

            val categoryId = repository.insertCategory(t.category.name)
            val sourceId = repository.insertFinancialSource(t.financialSource.name)
            val tagIds = t.tags.map { repository.insertTag(it.name) }


            val transactionId = repository.insertTransaction(
                t.transaction.copy(
                    categoryId = categoryId,
                    financialSourceId = sourceId
                ), tagIds
            )


//            tagIds.forEach { tagId ->
//                repository.insertTransactionTagCrossRef(
//                    TransactionTagCrossRef(
//                        transactionId = transactionId,
//                        tagId = tagId
//                    )
//                )
//            }

        }
    }
}
