package com.kazemieh.transactions.di

import com.kazemieh.transactions.TransactionsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val transactionsViewModelModule = module {
    viewModel {
        TransactionsViewModel(
            analytics = get(),
            transactionUseCaseGroup = get(),
            preferenceUseCases = get()
        )
    }
}
