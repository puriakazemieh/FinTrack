package com.kazemieh.backup_export

import com.kazemieh.common.model.Transaction
import org.dhatim.fastexcel.Workbook
import java.io.OutputStream

class ExcelExporter {
    fun exportTransactions(transactions: List<Transaction>, outputStream: OutputStream) {
        val workbook = Workbook(outputStream, "FinTrack", "1.0")
        val worksheet = workbook.newWorksheet("Transactions")

        // Header
        worksheet.value(0, 0, "ID")
        worksheet.value(0, 1, "Amount")
        worksheet.value(0, 2, "Description")
        worksheet.value(0, 3, "Date")
        worksheet.value(0, 4, "Type")

        // Data
        transactions.forEachIndexed { index, transaction ->
            val row = index + 1
            worksheet.value(row, 0, transaction.id)
            worksheet.value(row, 1, transaction.amount)
            worksheet.value(row, 2, transaction.description ?: "")
            worksheet.value(row, 3, transaction.timeStamp)
            worksheet.value(row, 4, transaction.type.name)
        }

        workbook.finish()
    }
}
