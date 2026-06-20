package com.kazemieh.backup_export.di

import com.kazemieh.backup_export.*
import org.koin.dsl.module
import org.koin.core.module.Module

actual val platformBackupExportModule: Module = module {
    single { ExcelExporter() }
    single { PdfExporter() }
    single<PlatformExporter> { AndroidPlatformExporter(get(), get(), get()) }
}
