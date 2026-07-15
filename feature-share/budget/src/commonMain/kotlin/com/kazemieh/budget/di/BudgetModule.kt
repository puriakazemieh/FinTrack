package com.kazemieh.budget.di

import com.kazemieh.budget.ui.add.AddBudgetViewModel
import com.kazemieh.budget.ui.list.BudgetViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val budgetModule = module {
    viewModel {
        BudgetViewModel(
            observeBudgetsWithProgressUseCase = get(),
            deleteBudgetUseCase = get(),
            addBudgetUseCase = get(),
            hasAnyBudgetsUseCase = get()
        )
    }
    viewModel {
        AddBudgetViewModel(
            addBudgetUseCase = get(),
            updateBudgetUseCase = get(),
            observeCategoriesUseCase = get(),
            observeTagsUseCase = get(),
            observeSourcesUseCase = get(),
            observeMostUsedCategoriesUseCase = get(),
            observeMostUsedSourcesUseCase = get()
        )
    }
}
