package com.kazemieh.backup_export

import android.graphics.Canvas
import com.kazemieh.common.model.Transaction
import android.graphics.pdf.PdfDocument
import java.io.OutputStream
import android.graphics.Paint
import android.graphics.Color
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.pdf_report_title
import fintrack.core.designsystem.generated.resources.pdf_transaction_row
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

class PdfExporter {
    fun exportTransactions(transactions: List<Transaction>, outputStream: OutputStream) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 12f

        var y = 40f
        canvas.drawText(
            runBlocking { getString(Res.string.pdf_report_title) },
            40f,
            y,
            paint
        )
        y += 30f

        transactions.take(20).forEach { // Limit to 20 for preview simplicity
            canvas.drawText(
                runBlocking {
                    getString(Res.string.pdf_transaction_row, it.id, it.amount, it.description.orEmpty())
                },
                40f,
                y,
                paint
            )
            y += 20f
        }

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }
}
