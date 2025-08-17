package com.kazemieh.financialsource.di

import com.kazemieh.financialsource.ui.FinancialSourceViewModel
import com.kazemieh.financialsource.ui.add.AddFinancialSourceViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val transactionFinancialSourceModule = module {
    viewModel {
        FinancialSourceViewModel(
            getAllFinancialSource = get()
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