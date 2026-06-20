package com.kazemieh.backup_export

import android.graphics.Canvas
import com.kazemieh.common.model.Transaction
import android.graphics.pdf.PdfDocument
import java.io.OutputStream
import android.graphics.Paint
import android.graphics.Color

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
        canvas.drawText("FinTrack - Transaction Report", 40f, y, paint)
        y += 30f

        transactions.take(20).forEach { // Limit to 20 for preview simplicity
            canvas.drawText("${it.id}: ${it.amount} - ${it.description}", 40f, y, paint)
            y += 20f
        }

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }
}
