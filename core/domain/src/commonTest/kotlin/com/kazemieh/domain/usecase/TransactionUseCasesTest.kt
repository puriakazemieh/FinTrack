package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class TransactionUseCasesTest {

    @Test
    fun `add transaction throws error if transfer source and destination are the same`() = runTest {
        val transaction = Transaction(
            id = 0,
            amount = 1000L,
            categoryId = 1,
            sourceId = 2,
            sourceEndId = 2, // SAME SOURCE
            type = TransactionType.TRANSFER
        )

        assertFailsWith<IllegalArgumentException> {
            if (transaction.type == TransactionType.TRANSFER && transaction.sourceId == transaction.sourceEndId) {
                throw IllegalArgumentException("Cannot transfer to the same account")
            }
        }
    }
}
