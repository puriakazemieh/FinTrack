package com.kazemieh.transaction.di

import com.kazemieh.transaction.ui.TransactionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionPresentationModule = module {
    viewModel {
        TransactionViewModel(
            transactionUseCases = get()
        )
    }
}
