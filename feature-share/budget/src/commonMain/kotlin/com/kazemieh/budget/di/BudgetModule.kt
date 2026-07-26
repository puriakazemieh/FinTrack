package com.kazemieh.budget.di

import com.kazemieh.budget.ui.add.AddBudgetViewModel
import com.kazemieh.budget.ui.list.BudgetViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val budgetModule = module {
    viewModelOf(::BudgetViewModel)
    viewModelOf(::AddBudgetViewModel)
}
