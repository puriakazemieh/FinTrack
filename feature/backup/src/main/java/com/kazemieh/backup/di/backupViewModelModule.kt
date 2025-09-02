package com.kazemieh.backup.di

import com.kazemieh.backup.ui.BackupViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val backupViewModelModule = module {
    viewModel {
        BackupViewModel(
            exportTransactions = get(),
            importTransactions = get(),
        )
    }
}