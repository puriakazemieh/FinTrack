package com.kazemieh.backup_export.di

import com.kazemieh.backup_export.BackupManager
import com.kazemieh.backup_export.ui.BackupExportViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.core.module.Module

expect val platformBackupExportModule: Module

val backupExportModule = module {
    includes(platformBackupExportModule)
    single { BackupManager(get()) }
    viewModel { BackupExportViewModel(get(), get()) }
}
