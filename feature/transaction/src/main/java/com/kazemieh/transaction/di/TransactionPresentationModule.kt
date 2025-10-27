package com.kazemieh.transaction.di

import com.kazemieh.transaction.ui.main.TransactionViewModel
import com.kazemieh.transaction.ui.add.AddTransactionViewModel
import com.kazemieh.transaction.ui.report.TransactionReportViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionPresentationModule = module {
    viewModel {
        TransactionViewModel(
            transactionUseCases = get()
        )
    }
}
val addTransactionPresentationModule = module {
    viewModel {
        AddTransactionViewModel(
            transactionUseCases = get()
        )
    }
}
val transactionReportViewModelModule = module {
    viewModel {
        TransactionReportViewModel(
            transactionUseCases = get()
        )
    }
}
