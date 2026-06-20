package com.kazemieh.backup_export

import com.kazemieh.common.model.Transaction
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream

class ExcelExporter {
    fun exportTransactions(transactions: List<Transaction>, outputStream: OutputStream) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Transactions")
        
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("ID")
        header.createCell(1).setCellValue("Amount")
        header.createCell(2).setCellValue("Description")
        header.createCell(3).setCellValue("Date")
        header.createCell(4).setCellValue("Type")

        transactions.forEachIndexed { index, transaction ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(transaction.id.toDouble())
            row.createCell(1).setCellValue(transaction.amount.toDouble())
            row.createCell(2).setCellValue(transaction.description ?: "")
            row.createCell(3).setCellValue(transaction.timeStamp.toDouble())
            row.createCell(4).setCellValue(transaction.type.name)
        }

        workbook.write(outputStream)
        workbook.close()
    }
}
