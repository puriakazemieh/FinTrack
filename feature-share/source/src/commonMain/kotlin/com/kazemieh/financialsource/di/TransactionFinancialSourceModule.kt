package com.kazemieh.financialsource.di

import com.kazemieh.financialsource.ui.add.AddSourceViewModel
import com.kazemieh.financialsource.ui.delete.DeleteSourceViewModel
import com.kazemieh.financialsource.ui.list.SourceViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionFinancialSourceModule = module {
    viewModel {
        SourceViewModel(
            analytics = get(),
            observeSourcesUseCase = get(),
            updateSourcePositionsUseCase = get()
        )
    }
}
val deleteSourceModule = module {
    viewModel {
        DeleteSourceViewModel(
            analytics = get(),
            deleteSourceUseCase = get(),
            observeSourceUseCase = get()
        )
    }
}
val transactionAddFinancialSourceModule = module {
    viewModel {
        AddSourceViewModel(
            analytics = get(),
            sourceUseCases = get()
        )
    }
}