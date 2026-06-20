package com.kazemieh.backup_export.di

import com.kazemieh.backup_export.*
import org.koin.dsl.module
import org.koin.core.module.Module

actual val platformBackupExportModule: Module = module {
    single<PlatformExporter> { 
        object : PlatformExporter {
            override suspend fun exportToExcel(transactions: List<com.kazemieh.common.model.Transaction>): String? = null
            override suspend fun exportToPdf(transactions: List<com.kazemieh.common.model.Transaction>): String? = null
        }
    }
}
