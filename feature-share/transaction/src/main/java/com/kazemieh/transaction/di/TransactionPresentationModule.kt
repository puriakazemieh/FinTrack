package com.kazemieh.transaction.di

import com.kazemieh.transaction.ui.main.TransactionViewModel
import com.kazemieh.transaction.ui.add.AddTransactionViewModel
import com.kazemieh.transaction.ui.delete.DeleteTransactionViewModel
import com.kazemieh.transaction.ui.report.TransactionReportViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionPresentationModule = module {
    viewModel {
        TransactionViewModel(
            transactionUseCaseGroup = get()
        )
    }
}
val addTransactionPresentationModule = module {
    viewModel {
        AddTransactionViewModel(
            transactionUseCaseGroup = get()
        )
    }
}
val transactionReportViewModelModule = module {
    viewModel {
        TransactionReportViewModel(
            transactionUseCaseGroup = get()
        )
    }
}
val transactionDeleteViewModelModule = module {
    viewModel {
        DeleteTransactionViewModel(
            transactionUseCaseGroup = get()
        )
    }
}
