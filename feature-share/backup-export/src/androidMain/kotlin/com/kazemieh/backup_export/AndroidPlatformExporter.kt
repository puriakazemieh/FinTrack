package com.kazemieh.backup_export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.kazemieh.common.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AndroidPlatformExporter(
    private val context: Context,
    private val excelExporter: ExcelExporter,
    private val pdfExporter: PdfExporter
) : PlatformExporter {

    override suspend fun exportToExcel(transactions: List<Transaction>): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, "fintrack_export.xlsx")
            val outputStream = FileOutputStream(file)
            excelExporter.exportTransactions(transactions, outputStream)
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun exportToPdf(transactions: List<Transaction>): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, "fintrack_report.pdf")
            val outputStream = FileOutputStream(file)
            pdfExporter.exportTransactions(transactions, outputStream)
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    override fun shareFile(path: String) {
        val file = File(path)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share Export")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    override fun shareText(content: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_TITLE, title)
        }
        val chooser = Intent.createChooser(intent, "Share Data")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
