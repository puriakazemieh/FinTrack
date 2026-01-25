package com.kazemieh.financialsource.di

import com.kazemieh.financialsource.ui.add.AddFinancialSourceViewModel
import com.kazemieh.financialsource.ui.delete.DeleteSourceViewModel
import com.kazemieh.financialsource.ui.list.SourceViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionFinancialSourceModule = module {
    viewModel {
        SourceViewModel(
            getAllSource = get()
        )
    }
}
val deleteSourceModule = module {
    viewModel {
        DeleteSourceViewModel(
            deleteSourceUseCase = get(),
            getSource = get()
        )
    }
}
val transactionAddFinancialSourceModule = module {
    viewModel {
        AddFinancialSourceViewModel(
            sourceUseCases = get()
        )
    }
}