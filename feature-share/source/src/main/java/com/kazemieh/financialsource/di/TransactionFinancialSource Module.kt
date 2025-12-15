package com.kazemieh.financialsource.di

import com.kazemieh.financialsource.ui.SourceViewModel
import com.kazemieh.financialsource.ui.add.AddFinancialSourceViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionFinancialSourceModule = module {
    viewModel {
        SourceViewModel(
            getAllSource = get()
        )
    }
}
val transactionAddFinancialSourceModule = module {
    viewModel {
        AddFinancialSourceViewModel(
            addFinancialSourceUseCase = get()
        )
    }
}