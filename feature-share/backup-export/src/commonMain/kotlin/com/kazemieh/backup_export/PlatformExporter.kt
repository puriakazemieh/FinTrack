package com.kazemieh.backup_export

import com.kazemieh.common.model.Transaction

interface PlatformExporter {
    suspend fun exportToExcel(transactions: List<Transaction>): String?
    suspend fun exportToPdf(transactions: List<Transaction>): String?
    fun shareFile(path: String)
    fun shareText(content: String, title: String)
}
