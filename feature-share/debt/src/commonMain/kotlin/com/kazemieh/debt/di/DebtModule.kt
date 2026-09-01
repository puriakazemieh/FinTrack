package com.kazemieh.debt.di

import com.kazemieh.debt.ui.add.AddDebtViewModel
import com.kazemieh.debt.ui.list.DebtViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val debtModule = module {
    viewModel {
        DebtViewModel(
            analytics = get(),
            debtUseCases = get()
        )
    }
    viewModel {
        AddDebtViewModel(
            analytics = get(),
            debtUseCases = get(),
            transactionUseCases = get()
        )
    }
}
